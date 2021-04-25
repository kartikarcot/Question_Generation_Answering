#Author: Rohan Joshi
import torch
import transformers
from transformers import BertForQuestionAnswering, DistilBertTokenizerFast
from transformers import BertTokenizer, DistilBertForQuestionAnswering
import json
from torch.utils.data import Dataset, DataLoader
from transformers import AdamW
from tqdm import tqdm
import torch.nn as nn
dir_name = "Sample_Questions"
class QA:
	def __init__(self):
		self.model = BertForQuestionAnswering.from_pretrained(".")
		self.tokenizer = BertTokenizer.from_pretrained(".")
		
	def printTokens(self,tokens, input_ids):
		for token, id in zip(tokens, input_ids):

			if id == self.tokenizer.sep_token_id:
				print(' ')

			print(token,":\t",id)

			if id == self.tokenizer.sep_token_id:
				print(' ')


	def answer(self,example):
		
		answerText = self.slidingWindowApproach(example)
		return answerText


	def slidingWindowApproach(self,example):
		N = len(example.reference)
		
		currMax = float('-inf')
		window_size = 350
		answerText = ""
		
		for start_index in range(0,N - window_size, window_size):
			sw_ref = example.reference[start_index:start_index+window_size]
			#print(example.question)
			#print("---\n",sw_ref)
			input_ids = self.tokenizer.encode(example.question, sw_ref)

			tokens = self.tokenizer.convert_ids_to_tokens(input_ids)
			#self.printTokens(tokens, input_ids)
			sep_index = input_ids.index(self.tokenizer.sep_token_id)
			num_seg_a = sep_index + 1
			num_seg_b = len(input_ids) - num_seg_a

			segment_ids = [0]*num_seg_a + [1]*num_seg_b
			xtoken = torch.tensor([segment_ids])
			#print("xtoken.shape = ",xtoken.shape)
			xq = torch.tensor([input_ids])
			outputs = self.model(xq, token_type_ids =xtoken)
			start_scores, end_scores = outputs.start_logits, outputs.end_logits
			#print(start_scores)
			#print(end_scores)
			#print(type(start_scores))
			startVals = torch.max(start_scores)
			endVals = torch.max(end_scores)
			if startVals + endVals > currMax:
				start = torch.argmax(start_scores)
				end = torch.argmax(end_scores)
				currMax = startVals + endVals
				answerText = ' '.join(tokens[start: end+1])

		return answerText


	def fine_tune(self):
		global q_file_names, dir_name
		contexts = []
		questions = []
		answers = []
		celoss = nn.CrossEntropyLoss()

		for param in self.model.bert.parameters():
			param.requires_grad = False
		optim = AdamW(self.model.parameters(), lr = 1e-7)

		######
		file_dict_list = []
		reference_list = []

		for i in range(len(q_file_names)):
			fname = dir_name+ "/"+q_file_names[i]+".json"
			with open(fname, 'rb') as f:
				file_dict = json.load(f)
				file_dict_list.append(file_dict)

			refname = dir_name+ "/"+q_file_names[i]+".txt"
			with open(refname,'r') as f:
				reference = f.read()
				reference_list.append(reference)
		assert len(reference_list) == len(file_dict_list)

		for i in range(len(reference_list)):
			file_dict = file_dict_list[i]
			reference = reference_list[i]

		num_epochs = 2
		for epoch in range(num_epochs):
			print("epoch = ",epoch)
			for i in tqdm(range(len(reference_list))):
				reference = reference_list[i]
				file_dict = file_dict_list[i]

				#print(reference)
				for _,v in file_dict.items():
					q = v['q']
					answer = v['a']
					ans_start_index = v['start_index']
					ans_end_index = v['end_index']
					#answer_stuff = {'answer': answer, 'start':ans_start_index, 'end':ans_end_index}

					N = len(reference)

					window_size = 350
					self.model.train()

					for start_index in range(0,N - window_size, window_size):
						if ans_start_index> start_index and ans_end_index< start_index+window_size:
							sw_ref = reference[start_index:start_index+window_size]
							new_start = ans_start_index - start_index
							new_end = ans_end_index - start_index
							#print(sw_ref[new_start:new_end])
							input_ids = self.tokenizer.encode(q, sw_ref)

							tokens = self.tokenizer.convert_ids_to_tokens(input_ids)

							sep_index = input_ids.index(self.tokenizer.sep_token_id)
							num_seg_a = sep_index + 1
							num_seg_b = len(input_ids) - num_seg_a

							segment_ids = [0]*num_seg_a + [1]*num_seg_b
							xtoken = torch.tensor([segment_ids])
							xq = torch.tensor([input_ids])
							outputs = self.model(xq, token_type_ids =xtoken)

							start_target = torch.tensor([new_start])
							end_target = torch.tensor([new_end])
							if start_target>outputs.start_logits.shape[1] or end_target>outputs.start_logits.shape[1]: continue
							#print("here")
							start = torch.argmax(outputs.start_logits)
							end = torch.argmax(outputs.end_logits)
							
							answerText = ' '.join(tokens[start: end+1])
							#print(answerText)

							loss = celoss(outputs.start_logits, start_target)+celoss(outputs.end_logits, end_target)
							loss.backward()
							optim.step()


						#exit(0)
						#start_scores, end_scores = outputs.start_logits, outputs.end_logits
		model_path = "full_bert_ft"
		self.model.save_pretrained(model_path)
		self.tokenizer.save_pretrained(model_path)
		#return contexts, questions, answers



			
q_file_names = ["set1_a1",
"set2_a1",
"set3_a1",
"set4_a1",
"set5_a1",
"set1_a2",
"set2_a2",
"set3_a2",
"set4_a2",
"set5_a2",
"set1_a3",
"set2_a3",
"set3_a3",
"set4_a3",
"set5_a3",
"set1_a4",
"set2_a4",
"set3_a4",
"set4_a4",
"set5_a4",
"set1_a5",
"set2_a5",
"set3_a5",
"set4_a5",
"set5_a5",
"set1_a6",
"set2_a6",
"set3_a6",
"set4_a6",
"set5_a6",
"set1_a7",
"set2_a7",
"set3_a7",
"set4_a7",
"set5_a7",
"set1_a8",
"set2_a8",
"set3_a8",
"set4_a8",
"set5_a8",
"set1_a9",
"set2_a9",
"set3_a9",
"set4_a9",
"set5_a9",
"set1_a10",
"set2_a10",
"set3_a10",
"set4_a10",
"set5_a10"]
def read_files():
	global q_file_names, dir_name
	contexts = []
	questions = []
	answers = []

	file_dict_list = []
	reference_list = []

	for i in range(len(q_file_names)):
		fname = dir_name+ "/"+q_file_names[i]+".json"
		with open(fname, 'rb') as f:
			file_dict = json.load(f)
			file_dict_list.append(file_dict)

		refname = dir_name+ "/"+q_file_names[i]+".txt"
		with open(refname,'r') as f:
			reference = f.read()
			reference_list.append(reference)
	assert len(reference_list) == len(file_dict_list)

	for i in range(len(reference_list)):
		file_dict = file_dict_list[i]
		reference = reference_list[i]


		#print(reference)
		for _,v in file_dict.items():
			q = v['q']
			answer = v['a']
			ans_start_index = v['start_index']
			ans_end_index = v['end_index']
			#answer_stuff = {'answer': answer, 'start':ans_start_index, 'end':ans_end_index}

			N = len(reference)

			window_size = 350

			for start_index in range(0, N - window_size, window_size):
				if ans_start_index>start_index and ans_end_index< start_index+window_size:
					context = reference[start_index: start_index+window_size]
					contexts.append(context)
					answer = v['a']
					curr_ans_start_index = v['start_index'] - start_index
					curr_ans_end_index = v['end_index'] - start_index
					answer_stuff = {'answer': answer, 'start':curr_ans_start_index, 'end':curr_ans_end_index}

					answers.append(answer_stuff)
					questions.append(q)

	return contexts, questions, answers


def add_token_positions(encodings, answers,tokenizer):
	start_positions = []
	end_positions = []

	for i in range(len(answers)):
		start_positions.append(encodings.char_to_token(i,answers[i]['start']))
		end_positions.append(encodings.char_to_token(i,answers[i]['end']-1))
		if start_positions[-1] is None:
			start_positions[-1] = tokenizer.model_max_length
		go_back = 1
		while end_positions[-1] is None:
			end_positions[-1] = encodings.char_to_token(i, answers[i]['end']-go_back)
			go_back +=1



	encodings.update({'start_positions': start_positions, 'end_positions': end_positions})



class QADataset(Dataset):
	def __init__(self, encodings):
		self.encodings = encodings


	def __getitem__(self, idx):
		
		return {key: torch.tensor(val[idx]) for key,val in self.encodings.items()}

	def __len__(self):
		return len(self.encodings.input_ids)


def train():
	tokenizer = DistilBertTokenizerFast.from_pretrained('distilbert-base-uncased')
	train_contexts, train_questions, train_answers = read_files()
	train_encodings = tokenizer( train_contexts, train_questions, truncation = True, padding=True)
	#print(tokenizer.decode(train_encodings['input_ids'][0]))
	add_token_positions(train_encodings, train_answers, tokenizer)
	print(train_encodings.keys())
	print(train_encodings)
	train_dataset = QADataset(train_encodings)
	model = DistilBertForQuestionAnswering.from_pretrained("distilbert-base-uncased")
	
	for param in model.distilbert.parameters():
		#print('-')
		param.requires_grad = False

	for p in model.qa_outputs.parameters():
		print(p.requires_grad)

	num_epochs = 5

	optim = AdamW(model.parameters(), lr = 1e-6)
	
	train_loader = DataLoader(train_dataset, batch_size = 8, shuffle=True)
	for epoch in range(num_epochs):

		model.train()
		total_loss = 0
		for batch in tqdm(train_loader):
			optim.zero_grad()

			input_ids = batch['input_ids']
			attention_mask = batch['attention_mask']
			start_positions = batch['start_positions']
			end_positions = batch['end_positions']
			outputs = model(input_ids, attention_mask=attention_mask, start_positions = start_positions, end_positions=end_positions)
			loss = outputs[0]

			loss.backward()
			optim.step()
			total_loss += loss.item()
		print("Epoch ",epoch,": ",loss.item())

	model_path = "bert_ft"
	model.save_pretrained(model_path)
	tokenizer.save_pretrained(model_path)



def test():
	global q_file_names, dir_name
	reffile = "set1_a3"
	refname = dir_name+ "/"+reffile+".txt"
	with open(refname,'r') as f:
		reference = f.read()
	model_path = "bert_ft"
	#model = DistilBertForQuestionAnswering.from_pretrained(model_path)
	#tokenizer = DistilBertTokenizerFast.from_pretrained(model_path)
	tokenizer = DistilBertTokenizerFast.from_pretrained('distilbert-base-uncased')
	model = DistilBertForQuestionAnswering.from_pretrained("distilbert-base-uncased")

	q = ["When is the exact beginning of the New Kingdom?"]
	questions = []
	contexts = []
	N = len(reference)

	window_size = 350
	model.eval()
	currMax = float('-inf')
	for start_index in range(0, N - window_size, window_size):
		context = [reference[start_index: start_index+window_size]]
		encodings = tokenizer(context, q)
		#print(encodings.keys())
		outputs = model(torch.tensor(encodings['input_ids']), torch.tensor(encodings['attention_mask']))
		start_pred = torch.argmax(outputs['start_logits'], dim=1)
		end_pred = torch.argmax(outputs['end_logits'], dim=1)
		#print(start_pred, end_pred)
		print("answer = ",context[0][start_pred:end_pred])
		start_scores, end_scores = outputs.start_logits, outputs.end_logits
		#print(start_scores)
		#print(end_scores)
		#print(type(start_scores))
		startVals = torch.max(start_scores)
		endVals = torch.max(end_scores)
		if startVals + endVals > currMax:
			start = torch.argmax(start_scores)
			end = torch.argmax(end_scores)
			currMax = startVals + endVals
			#print(start, "\t",end)
			#print("here ")
			answerText = ' '.join(context[0][start: end+1])
		
	#print(answerText)



		#answers.append(answer_stuff)
		#questions.append(q)

	



def slidingWindowApproach(example, tokenizer):
		N = len(example.reference)
		
		currMax = float('-inf')
		window_size = 350
		
		for start_index in range(0,N - window_size, window_size):
			sw_ref = example.reference[start_index:start_index+window_size]
			#print(example.question)
			#print("---\n",sw_ref)
			input_ids = tokenizer.encode(example.question, sw_ref)

			tokens = tokenizer.convert_ids_to_tokens(input_ids)
			#self.printTokens(tokens, input_ids)
			sep_index = input_ids.index(self.tokenizer.sep_token_id)
			num_seg_a = sep_index + 1
			num_seg_b = len(input_ids) - num_seg_a

			segment_ids = [0]*num_seg_a + [1]*num_seg_b
			xtoken = torch.tensor([segment_ids])
			#print("xtoken.shape = ",xtoken.shape)
			xq = torch.tensor([input_ids])
			outputs = model(xq, token_type_ids =xtoken)
			start_scores, end_scores = outputs.start_logits, outputs.end_logits
			#print(start_scores)
			#print(end_scores)
			#print(type(start_scores))
			startVals = torch.max(start_scores)
			endVals = torch.max(end_scores)
			if startVals + endVals > currMax:
				start = torch.argmax(start_scores)
				end = torch.argmax(end_scores)
				currMax = startVals + endVals
				answerText = ' '.join(tokens[start: end+1])

		return answerText


		







'''
if __name__=='__main__':
	#train()
	
	qa = QA()
	qa.fine_tune()
'''



