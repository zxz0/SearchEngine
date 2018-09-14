from flask import Flask, g, render_template, request, flash, redirect, url_for, Markup
from urllib.request import urlopen
import urllib.parse
import json
import spell_corrector
import re
from bs4 import BeautifulSoup

app = Flask(__name__)
app.secret_key = 'something secret'

solr_core_name = 'news'
map_file = 'data/mapNYTimesDataFile.csv'

# Get id-url mapping
map = {}    # dict{id: url}
try:
    with open(map_file, 'r') as f:
        tuple = f.readline()
        while tuple:
            data = tuple.strip().split(',') # [id, url]
            map[data[0]] = data[1]
            tuple = f.readline()
        print('data map file loaded!')
except IOError as ioerr:
    print(str(ioerr))

# Homepage
@app.route('/')
def index():
    return render_template('index.html')

# Get search results for the query
@app.route('/results', methods=['GET'])
def get_result():
    error = None
    # Get the query and method
    query = request.args.get('query', '').strip()   # fallback: ''
    method = request.args.get('sorting_method', '')
    
    app.logger.info("Getting query: {} using method: {}".format(query, method))

    # Handle invalid situations
    if not method:
        error = 'Method is required.'
    elif not query:
        error = 'Query is required.'
    elif method != 'default' and method != 'page_rank':
        error = 'Method is invalid.'
    elif not valid_query(query):
        error = 'Query is invalid.'

    if error is None:
        # Get results
        if method == 'default':
            connection = urlopen('http://localhost:8983/solr/{}/select?q={}&wt=json'.format(solr_core_name, urllib.parse.quote(query)))
        else:   # method == 'page_rank'
            connection = urlopen('http://localhost:8983/solr/{}/select?q={}&sort={}&wt=json'.format(solr_core_name, urllib.parse.quote(query), urllib.parse.quote('pageRankFile desc')))

        # Parse results
        response = json.load(connection)['response']
        data = {'numFound': response['numFound'], 'query': query, 'method': method, 'docs': []}
        data_entries = response['docs']
        lower_query = query.lower()
        for entry in data_entries:
            current_data = {}
            current_data['id'] = entry['id'][entry['id'].rfind('/') + 1:]
            current_data['url'] = map[current_data['id']]

            try:
                current_data['title'] = entry['title'][0] or 'Title'    # fall back
            except KeyError as ky:
                app.logger.error("Failed to get title in file: {}".format(current_data['id']))
                current_data['title'] = 'no title'
            
            # Generate snippet
            try:
                current_data['snippet'] = get_snippet(lower_query, entry['id']) or entry['description'][0]
                # Bold the query terms in the sentence (raw HTML)
                current_data['snippet'] = Markup(re.sub(r'\b{}\b'.format(query), wrap_bold, current_data['snippet'], flags=re.I))
            except KeyError as ky:
                app.logger.error("Failed to get description in file: {}".format(current_data['id']))
                current_data['snippet'] = 'no snippet'
            
            data['docs'].append(current_data)

        # Check spelling
        corr = get_correction(lower_query)
        if corr != lower_query:
            data['corr'] = {'name': corr, 'url_suffix': '?query={}&sorting_method={}'.format(urllib.parse.quote(corr), urllib.parse.quote(method))}

        return render_template('result_page.html', results = data)

    flash(error)

    return redirect(url_for('index'))

# Get autocomplete suggestions for the query
@app.route('/suggestions', methods=['GET'])
def get_suggestion():
    query = request.args.get('term', '').lstrip()
    
    app.logger.info("Getting autocomplete suggestion for the last term of query: {}".format(query))
    
    if query[-1] == ' ':    # no suggestion needed if space
        return '[]'
    else:
        query = query.rstrip()
        terms = query.split()
        term = terms[-1].lower()    # split previous words and current word used to get suggestions
        prefix_str = ' '.join(terms[:-1])

        connection = urlopen('http://localhost:8983/solr/{}/suggest?q={}&wt=json'.format(solr_core_name, term))
        suggestions = json.load(connection)['suggest']['suggest'][term]['suggestions']
        if prefix_str:  # add previous words if any
            candidates = [prefix_str + ' ' + suggestion['term'] for suggestion in suggestions if re.match('^[a-zA-Z]+$', suggestion['term'])]
        else:
            candidates = [suggestion['term'] for suggestion in suggestions if re.match('^[a-zA-Z]+$', suggestion['term'])]

    return str(candidates).replace("'", '"')    # jQuery autocomplete widgets needs quotation mark ", not '

# Validate the query
def valid_query(query):
    return query != '*'

# Get correction for the query word by word
def get_correction(lower_query):
    app.logger.info("Getting correction for query: {} in lower case".format(lower_query))
    corr = ''
    terms = lower_query.split()
    for term in terms:
        corr += spell_corrector.correction(term) + ' '

    return corr[:-1]    # delete the space at the end

# Get snippet for the query from the file
def get_snippet(lower_query, file_path):
    app.logger.info("Getting snippet for query: {} in file: {}".format(lower_query, file_path))
    soup = BeautifulSoup(open(file_path))
    query_terms = re.compile(r'\b{}\b'.format(lower_query))
    
    for para in soup.find_all('p', {'class': 'css-1i0edl6 e2kc3sl0'}):  # meaningful content
        content = para.string
        # Paragraph as snippet
        if content and re.search(query_terms, content.lower()):
            return(content)

        # Sentence as snippet
#        if content:
#            for sentence in content.split('.'):
#                if re.search(query_terms, sentence.lower()):
#                    return(sentence)

    return(None)

# Bold the query related text in HTML tags
def wrap_bold(matchobj):
    current_match_term = str(matchobj.group())  # == group(0), the whole match
    app.logger.info("Making query: {} bold in the snippet".format(current_match_term))
    return '<strong>{}</strong>'.format(current_match_term)
