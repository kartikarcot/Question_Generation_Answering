
from pycorenlp import StanfordCoreNLP
#import stanza
#from stanza.server import CoreNLPClient
from nltk.tree import Tree
import copy
import requests
import json
import os



class classifyQuestion():

    def __init__(self):
        self.params = {'properties':'{"annotators": "parse"}'}
        self.url = 'http://localhost:9000/'
	    





    #Recursive method to determine clause phrase
    def checkConstituency(self, tree):
        if(len(tree)==0):
            return "None"
        for child in tree:
            if isinstance(child, Tree):
                if child.label() == 'SQ' or child.label() == 'SBARQ':
                    return child.label() 
                result = self.checkConstituency(child)
                if result == 'SQ' or result == 'SBARQ':
                    return result
        return "None"
            
    #SQ: Indirect / Yes_No Question
    #SBARQ: Direct/WH Question
    #None: Not a question
    def QuestionType(self, text):


        result = []

        r = requests.post(self.url, data=text.encode('utf-8'), params=self.params, timeout=150000000)
        data = json.loads(r.text)
        #print(data["sentences"][0]["parse"])
        constituency_parse = Tree.fromstring(data["sentences"][0]["parse"])
        temp = copy.deepcopy(constituency_parse)
        result.append(self.checkConstituency(temp))
        return result


if __name__=='__main__':
    classifyQuestion_obj = classifyQuestion()
    with open("questions1.txt", 'r', encoding='utf-8') as f:
                questions = []
                sentences = f.readlines()
                for sentence in sentences:
                    result = classifyQuestion_obj.QuestionType(sentence)
                    questions.append((sentence, result))

    print(questions)



        


 