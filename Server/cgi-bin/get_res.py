#! /usr/local/bin/python3
# Zixuan Zhang
# 2017/04/05

# get_res:
# receive query from client, using Solr to generate top 10 results (in Lucene default and page-rank score order), format and send back to client

import cgi
import cgitb
from SolrClient import SolrClient
import form
import json
import websitemodel
import spell_corr

# get the query
cgitb.enable()
form_data = cgi.FieldStorage()
query = form_data['query'].value
method = form_data['method'].value

# get the terms
terms = query.split()

# get the corrections
corrs = []
for term in terms:
    corrs.append(spell_corr.correction(term.lower()))

# judge if it there is misspelling
flag = 0
corr_str = ''
for i in range(len(terms)):
    corr_str += corrs[i] + ' '
    if terms[i].lower() != corrs[i]:
        flag = 1
corr_str = corr_str.strip()

solr = SolrClient('http://localhost:8983/solr')

if method == 'default':
    # send the request and get the response from Solr
    response = solr.query('myexample',{
                        'q':query,
                     })

elif method == 'page_rank':
    response = solr.query('myexample',{
                          'q':query,
                          'sort':'pageRankFile desc'
                          })

# title(clickable),url(clickable), id, description should be shown for each query.
res_dict = json.loads(response.get_json())
titles = response.get_field_values_as_list('title')
ids = response.get_field_values_as_list('id')

# get id from ids (full paths)
ids = [id[id.rfind('/') + 1:] for id in ids]

# get urls from csv file
all_websites = websitemodel.get_addr_from_file()
urls = [all_websites[id] for id in ids]

# form the html
print(form.start_response())
print(form.header('HW4 Search Engine'))
print(form.h('query you input: ' + query))
if flag == 1:
    print(form.h('do you mean: '),)
    print(form.link('http://localhost:8080/cgi-bin/get_res.py?method=' + method + '&query=' + corr_str.replace(' ', '+'), corr_str, 'h2'))

res_num = response.get_results_count()
print(form.h('Best (' + method + ') ' + str(res_num) + ' results for your query are:'))

for i in range(res_num):
    title = str(titles[i][0])
    snippet = ''
    print(form.link(str(urls[i]), str(title), 'h3'))
    print(form.link(str(urls[i]), str(urls[i]), 'nor'))
    print(form.nor('id: ' + str(ids[i])))
    if 'description' in res_dict['response']['docs'][i]:
        desc = str(res_dict['response']['docs'][i]['description'][0])
        # check if query terms are in the description
        flg = 1
        for j in range(len(terms)):
            if terms[j] not in desc.lower():
                flg = 0
                break
        # if is, use the description as snippet
        if flg:
            snippet = desc
        print(form.para(desc))
    else:
        print(form.para('no description'))

    # check if query terms are in the title
    flg = 1
    for j in range(len(terms)):
        if terms[j] not in title.lower():
            flg = 0
            break
    if flg:
        snippet = title

    # if the query terms are neither in the title nor in the description
    if not snippet:
        snippet = websitemodel.generate_snippet(terms, ids[i])

    print(form.para(snippet))
    print(form.para(''))

# get the results for page-rank score
'''response = solr.query('myexample',{
                    'q':query,
                    'sort':'pageRankFile desc'
                    })
res_dict = json.loads(response.get_json())
titles = response.get_field_values_as_list('title')
ids1 = response.get_field_values_as_list('id')
descs = response.get_field_values_as_list('description')

# get id from resourcename
ids1 = [id1[id1.rfind('/') + 1:] for id1 in ids1]

# get urls from retrieved dictionary
urls = [all_websites[id1] for id1 in ids1]

# get overlap
overlap = set(ids).intersection(ids1)

res_num = response.get_results_count()

# form the html
print(form.h('Best (pagerank) ' + str(res_num) + ' results for your query are:'))

for i in range(res_num):
    print(form.link(str(urls[i]), str(titles[i][0]), 'h3'))
    print(form.link(str(urls[i]), str(urls[i]), 'nor'))
    print(form.nor('id: ' + str(ids1[i])))
    if 'description' in res_dict['response']['docs'][i]:
        print(form.para(str(res_dict['response']['docs'][i]['description'][0])))
    else:
        print(form.para('no description'))
    print(form.para(''))

print(form.para('overlap length: ' + str(len(overlap))))
print(form.para('overlap files: ' + str(overlap)))'''
