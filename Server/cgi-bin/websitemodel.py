#! /usr/local/bin/python3
# Zixuan Zhang
# 2017/04/05

import tika
from tika import parser
import re

# website model:
# get data from file
# get dictionary of id: url from csv file; generate snippets based on word and file

#from HTMLParser import HTMLParser

# get dict{id: url} from file
def get_addr_from_file():
    all_websites = {}   # all_websites: {id: address}
    try:
        with open('data/mapNYTimesDataFile.csv', 'r') as f:
            tuple = f.readline()
            while tuple:
                data = tuple.strip().split(',')
                all_websites[data[0]] = data[1]
                tuple = f.readline()
    except IOError as ioerr:
        print('File error (get_from_store): ' + str(ioerr))

    return(all_websites)

# generate snippets based on word and file
def generate_snippet(words, file):
    p=re.compile('[\f\r\t\v]+')
    parsed = parser.from_file('data/NYTimesDownloadData/' + file)
    # get content
    lines = re.sub(p, ' ', parsed["content"].strip())
    lines_li = lines.split('\n')
    # find line have all the query words
    for line in lines_li:
        flg = 1
        for i in range(len(words)):
            if words[i] not in line.lower():
                flg = 0
                break
        if flg:
            #if (len(line) > 160)
            return(line)
    return('no snippet')

    '''h = html2text.HTML2Text()
    h.ignore_links = True
    try:
        with open('data/NYTimesDownloadData/' + file, 'r') as f:
            tuple = f.readline().strip()
            while tuple:
                line = h.handle(tuple).strip()
                if line.find(word) != -1:
                    return line
                tuple = f.readline()
    except IOError as ioerr:
        print('File error (generate_snippet): ' + str(ioerr))

    return("no snippet")'''
