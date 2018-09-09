# get weight of specified websites based on PageRank
# python3 pagerank.py

import re
import networkx as nx
import os
import sys
from bs4 import BeautifulSoup
import configparser

# get dict{url: id} from file
def get_from_store(map_file):
    all_urls = {}   # all_urls: {url: id}
    all_ids = {}    # all_ids: {id: url}
    try:
        with open(map_file, 'r') as f:
            tuple = f.readline()
            while tuple:
                data = tuple.strip().split(',')
                all_ids[data[0]] = data[1]
                all_urls[data[1]] = data[0]
                tuple = f.readline()
    except IOError as ioerr:
        print("File error (get_from_store): " + str(ioerr))
    
    return(all_urls, all_ids)

if __name__ == '__main__':
    config_file = 'config.ini'
    
    if os.path.exists(config_file):
        config = configparser.ConfigParser()
        config.read(config_file)
        map_file = config.get('DEFAULT', 'map_file')
        result_file = config.get('DEFAULT', 'result_file')
        html_dir = config.get('DEFAULT', 'html_dir')
    else:
        print("Set the config file with paths of map_file, result_file and html_dir under DEFAULT")
        exit()

    # initialization
    all_urls, all_ids = get_from_store(map_file)
    G=nx.DiGraph()

    # get files in directory
    list_dirs = os.walk(html_dir)
    for root, dirs, files in list_dirs:
        # create edges according to each file
        print("finding out links and adding to the graph...")
        i = 0
        for f in files:
            current_id = f
            full_path = os.path.join(root, f)
            with open(full_path, 'r', encoding = 'utf-8') as web_file:
                # get out links
                data = web_file.read()
                '''pattern = re.compile('<a href=\"(.*?)\"', re.S);
                links = re.findall(pattern, data)
                # for links in the table, add edge with node name: id of the website
                for link in links:
                    #print(link)
                    if link in all_urls:
#                        i += 1
                        G.add_edge(current_id, all_urls[link])'''
                soup = BeautifulSoup(data, "html.parser")
                links = soup.find_all('a')
                
                for tag in links:
                    link = tag.get('href',None)
                    if link is not None:
                        if link in all_urls:
                            i += 1
                            G.add_edge(current_id, all_urls[link])

    pagerank_score = nx.pagerank(G, alpha=0.85, personalization=None, max_iter=30, tol=1e-06, nstart=None, weight='weight',dangling=None)

    with open(result_file, 'w', encoding = 'utf-8') as score_file:
        print("writing to file...")
        for (id, score) in pagerank_score.items():
            score_file.write(html_dir + str(id))
            score_file.write('=' + str(score) + '\n')
