#! /usr/local/bin/python3
# Zixuan Zhang
# 2017/04/25

# get_suggest:
# receive partial query from client, using Solr to generate top 5 auto-complete suggestion

import cgi
from SolrClient import SolrClient
import os
import json
import form
import re

# get the parameter (partial query)
query_str = os.environ['QUERY_STRING']
terms = query_str[query_str.find('=') + 1:].split('%20')
print(form.start_response())
term = terms[-1]
prefix_str = ' '.join(terms[:-1])

# send the request and get the response from Solr (suggest)
solr = SolrClient('http://localhost:8983/solr')
response = solr.query('myexample', {
                      'q':term,
                      }, 'suggest')

# get json result
res_dict = json.loads(response.get_json())
# !! update: filter the urls KO
# !! update: add prefix
# get suggestions
iterms = res_dict['suggest']['suggest'][term]['suggestions']
candidates = [prefix_str + ' ' + iterm['term'] for iterm in iterms if re.match('^[a-zA-Z]+$', iterm['term'])]
#for candidate in candidates:
#    print(terms, ' ')
for candidate in candidates:
#    print('<li onclick="fill(' + candidate + ');">' + candidate + '</li>')
    print('<li class="autoli">' + candidate + '</li>')
