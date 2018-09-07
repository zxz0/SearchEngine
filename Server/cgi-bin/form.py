#! /usr/local/bin/python3
# Zixuan Zhang
# 2017/04/05

# form:
# functions to form html file

from string import Template

def start_response(type = 'text/html'):
    return('Content-type: ' + type + '\n\n')

def header(cu_title):
    with open('templates/header.html') as f:
        text = f.read()
    header = Template(text)
    return(header.substitute(title = cu_title))

def h(text, level = 2):
    return('<h' + str(level) + '>' + text + '</h' + str(level) + '>')

def para(para_text):
    return('<p>' + para_text + '</p>')

def nor(text):
    return(text)

def link(link_add, link_text, format):
    sen = '<a href="' + link_add + '" target="_blank">' + link_text + '</a>'
    if format == 'h3':
        return(h(sen, 3))
    elif format == 'nor':
        return(sen)
    elif format == 'para':
        return(para(sen))
    elif format == 'h2':
        return(h(sen))
