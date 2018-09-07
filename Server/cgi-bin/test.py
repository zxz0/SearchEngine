#! /usr/local/bin/python3
# Zixuan Zhang
# 2017/04/25

# test:
# test unit

#import spell_corr
import html2text
import re
import tika
from tika import parser

if __name__ == "__main__":
    p=re.compile('[\f\r\t\v]+')
#    h = html2text.HTML2Text()
#    h.ignore_links = True
#    try:
#        with open('/Users/zzx/Desktop/CSCI572_Information_Retrieval_and_Web_Search_Engines/homework/[0426]homework5/code/data/NYTimesDownloadData/0a1a4dfd-c645-4118-a29d-45c38028b984.html', 'r') as f:
#            tuple = f.readline().strip()
#            tuple = re.sub(p, '', tuple)
#            while tuple:
#                #line = re.sub(p, ' ', h.handle(tuple).strip())
#                line = h.handle(tuple).strip()
#                if line.find('beneficios') != -1:
#                    print(line)
#                    break
#                tuple = f.readline()
#    except IOError as ioerr:
#        print('File error (generate_snippet): ' + str(ioerr))

    parsed = parser.from_file('/Users/zzx/Desktop/CSCI572_Information_Retrieval_and_Web_Search_Engines/homework/[0426]homework5/code/data/NYTimesDownloadData/1cfd3428-713b-45a4-ae97-31612252752d.html')
    print(parsed["metadata"])
    lines = re.sub(p, ' ', parsed["content"].strip())
#    print(lines)
    lines_li = lines.split('\n')

    words = ['Donal']

    for line in lines_li:
        print(line)
        flg = 1
        for i in range(len(words)):
            if words[i] not in line:
                flg = 0
                print('no: ' + line)
                break
        print(flg)
        if flg:
            #if (len(line) > 160)
            print('yes: ' + line)
            break

#    for line in lines_li:
#        if 'Donal' in line:
#            print(line)
#            break
