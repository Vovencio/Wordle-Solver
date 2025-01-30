import os
import random
import math
from collections import defaultdict

folder_path = 'Words'

# Parameter n for other algorithm
n = 5

dictionary = []

# Letter To Id
def lti(letter):
	return ord(letter)-97

# Load Words
with open("complete.txt", 'r') as file:
	for word in file:
		word = word.strip()
		add = True
		# Check if only alphabetical symbols are present
		for i in range(5):
			if 0 < lti(word[i]) > 26:
				add = False
		if add:
			dictionary.append(word)

print("Words successfully loaded. Words:", len(dictionary))

# Return the word list with all words without an amount of a letter
def amountLetter(letter, num, words):
	new_words = []
	for word in words:
		if word.count(letter) == num:
			new_words.append(word)
	return new_words

# Return the word list without a letter at a place with a specified minimum
def placeLetter(letter, place, minimum, words):
	new_words = []
	for word in words:
		if word.count(letter) >= minimum and word[place] != letter:
			new_words.append(word)
	return new_words

# Return the word list with a letter at a place
def rightLetter(letter, num, words):
	new_words = []
	for word in words:
		if word[num] == letter:
			new_words.append(word)
	return new_words

# 0 = Gray, 1 = Yellow, 2 = Green'

# Convert a list of ints (len=5) to a limitation of possible words
def limitWords(answer, word, words):
	new_words = words[:]
	for i in range(5):
		match answer[i]: 
			case 0:
				count = 0
				for j in range(5):
					if i != j:
						if word[i] == word[j] and answer[j] != 0:
							count+=1
				new_words = amountLetter(word[i], count, new_words)
			case 1:
				count = 1
				for j in range(5):
					if i != j:
						if word[i] == word[j] and answer[j] != 0:
							count+=1
				new_words = placeLetter(word[i], i, count, new_words)
			case 2:
				new_words = rightLetter(word[i], i, new_words)
	return new_words

# Create an answer array with a goal word and a given word
def answerArray(word, goal, debug=False):
	answer = [0] * 5
	goal_amounts = [0] * 26
	
	for i in range(5):
		goal_amounts[lti(goal[i])] += 1
	
	for i in range(5):
		if word[i] == goal[i]:
			answer[i] = 2
			goal_amounts[lti(goal[i])] -= 1
	
	for i in range(5):
		if word[i] != goal[i] and goal_amounts[lti(word[i])] > 0 and answer[i] ==0:
			answer[i] = 1
			goal_amounts[lti(word[i])] -= 1
	
	#if debug:
	#	print(goal_amounts, word)
	
	return answer

# Score a word with an amount of possibility pruning
def scoreWordOld(word, words):
    pattern_counts = defaultdict(int)
    
    for potential_goal in words:
        pattern = tuple(answerArray(word, potential_goal))
        pattern_counts[pattern] += 1
    
    # Calculate entropy: sum(p * log(p)) for each pattern
    score = 0
    total = len(words)
    
    for count in pattern_counts.values():
        probability = count / total
        score -= probability * math.log2(probability)
    
    return score
  
def scoreWord(word, words):
	score = 0
	# p stands for possible goal word
	for p in words:
		score += len(limitWords(answerArray(word, p), word, words))**2
	return score

def bestWord(words):
	rated = []
	for word in words:
		rated.append([scoreWord(word, words), word])
	rated.sort(key = lambda x: x[0])
	return rated[:5]

def bestUnknown(unk, words):
	rated = []
	for word in dictionary:
		score = 0
		for letter in unk:
			if letter in word:
				score -= 1
		rated.append([score, word])
	rated.sort(key = lambda x: x[0])
	rated = rated[:20]
	
	for word in rated:
		word[0] = scoreWord(word[1], words)
	
	rated.sort(key = lambda x: x[0])
	
	return rated[:5]

# Get the amount of possible letters, which weren't checked for'
def unknown(words, greens):
	unk = []
	for word in words:
		for letter in word:
			if not letter in greens and not letter in unk:
				unk.append(letter)
	return unk

def main():
	# Greens are the letters not needing to be checked
	greens = []
	
	first = ['sport', 'adieu']
	words = dictionary[:]

	# Generate 100 random test cases
	random_words = [random.choice(dictionary) for _ in range(100)]
	random_answers = [[random.randint(0, 2) for _ in range(5)] for _ in range(100)]

	# Initialize an empty list to store expected lengths
	expected_lengths = []

	# Iterate over the generated words and answers to compute expected lengths
	for i in range(100):
	    answer = random_answers[i]
	    word = random_words[i]
	    expected_length = len(limitWords(answer, word, dictionary))  # Compute the expected length
	    expected_lengths.append(expected_length)

	# Print Java-style arrays
	print("int[][] ansArrays = {")
	for answer in random_answers:
	    print("    {" + ",".join(map(str, answer)) + "},")
	print("};\n")
	
	print("String[] words = {")
	for word in random_words:
	    print(f'    "{word}",')
	print("};\n")
	
	print("int[] expectedLengths = {")
	for length in expected_lengths:
	    print(f"    {length},")
	print("};")

	print("Please enter the words sport and adieu.")
	a = []
	for i in first:
		a.append(input(f'Answer from {i}: '))
	for i in range(2):
		ans = [int(j) for j in a[i]]
		words = limitWords(ans, first[i], words)
		
		for j in range(5):
			if not first[i][j] in greens:
				greens.append(first[i][j])
	
	
	
	while len(words) > 1:
		print(len(words))
		unk = unknown(words, greens)
		
		print(sorted(bestUnknown(unk, words)+bestWord(words), key = lambda x: x[0]))
		word = input("Word entered: ")
		ans = [int(j) for j in input("Answer received: ")]
		words = limitWords(ans, word, words)
		for i in range(5):
			if not word[i] in greens:
				greens.append(word[i])
	print(words)

def goThroughOne(gord):
	steps = 2
	greens = []
	words = dictionary[:]
	
	for i in ['sport', 'adieu']:
		ans = answerArray(i, gord, debug=True)
		words = limitWords(ans, i, words)
		for j in range(5):
			if not i[j] in greens:
				greens.append(i[j])
	
	while len(words) > 1:
		steps +=1
		unk = unknown(words, greens)
		best_words = sorted(bestUnknown(unk, words)+bestWord(words), key = lambda x: x[0])
		word = best_words[0][1]
		ans = answerArray(word, gord, debug=True)
		words = limitWords(ans, word, words)
		for i in range(5):
			if not word[i] in greens:
				greens.append(word[i])
	if len(words) == 0:
		return [999, gord]
	return [steps, gord]

def goThroughAll():
	rated = []
	for word in dictionary:
		rated.append(goThroughOne(word))
		print(word, 'is done')
	rated.sort(key = lambda x: x[0], reverse=True)
	with open("Worst.txt" , "w") as file:
		file.write(str(rated))

'''
def bake():
	greens = ['s','p','o','r','t','a','d','i','e','u']
	fwords = ['sport','adieu']
	start = False
	for a in range(3):
		for b in range(3):
			for c in range(3):
				for d in range(3):
					for e in range(3):
						for f in range(3):
							for g in range(3):
								for h in range(3):
									for i in range(3):
										for j in range(3):
											ans = [[a,b,c,d,e],[f,g,h,i,j]]
											name = 'Baked/' + str(ans[0] + ans[1]).replace(',','').replace('[','').replace(']','').replace(' ','') +'.txt'
											if not start:
												if os.path.exists(name):
													continue
												else:
													start = True
											word = ''
											words = dictionary[:]
											for _ in range(2):
												words = limitWords(ans[_], fwords[_], words)
											unk = unknown(words, greens)
											if len(words) > 0:
												best_words = sorted(bestUnknown(unk, words)+bestWord(words), key = lambda x: x[0])
												word = best_words[0][1]
											
												with open(name , "w") as file:
													file.write(word)
												#print(name)
'''		
while True:
	main()
#bake()
#goThroughAll()