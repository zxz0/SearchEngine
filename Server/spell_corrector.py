#! /usr/local/bin/python3
# Zixuan Zhang
# 2017/04/24
# source code from: http://norvig.com/spell-correct.html

# Spelling Corrector:
# correct spelling according to big.txt (modified)

import re
from collections import Counter

def words(text): return re.findall(r'\w+', text.lower())

WORDS = Counter(words(open('data/big.txt', 'r').read()))

# Probability of 'word'.
def P(word, N = sum(WORDS.values())):
    return WORDS[word] / N

# Most probable spelling correction for word.
def correction(word):
    return max(candidates(word), key = P)

# Generate possible spelling corrections for word.
def candidates(word):
    return (known([word]) or known(edits1(word)) or known(edits2(word)) or [word])  # if no suggestion within edit distance 2, treat as known, return the original word

# The subset of 'words' that appear in the dictionary of WORDS.
def known(words):
    return set(w for w in words if w in WORDS)

# All edits that are one edit away from 'word'.
def edits1(word):
    letters    = 'abcdefghijklmnopqrstuvwxyz'
    splits     = [(word[:i], word[i:])    for i in range(len(word) + 1)]
    deletes    = [L + R[1:]               for L, R in splits if R]
    transposes = [L + R[1] + R[0] + R[2:] for L, R in splits if len(R)>1]
    replaces   = [L + c + R[1:]           for L, R in splits if R for c in letters]
    inserts    = [L + c + R               for L, R in splits for c in letters]
    return set(deletes + transposes + replaces + inserts)

# All edits that are two edits away from 'word'.
def edits2(word):
    return (e2 for e1 in edits1(word) for e2 in edits1(e1))
