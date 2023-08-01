
def find_max(numbers):
    return max(numbers)



def is_palindrome(word):
    # Remove spaces and convert to lowercase
    word = word.replace(" ", "").lower()
    
    # Check if the word is a palindrome
    reversed_word = word[::-1]
    if word == reversed_word:
        return True
    else:
        return False
    









def find_min(numbers):
    return min(numbers)




if __name__ == "__main__":
    numbers = [2, 5, 8, 1, 9, 3, 6, 4, 7, 0]
    print("Generated numbers:", numbers)
    print("Max value:", find_max(numbers))
    print("Min value:", find_min(numbers))
    word = "level"
    print("Is '{}' a palindrome?".format(word), is_palindrome(word))