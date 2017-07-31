# The bigoquiz.com REST API

The server provides the following REST resources, using JSON

## Quiz

### Get all quizzes

&lt;base-uri&gt;/quiz/

For instance:
http://bigoquiz.com/api/quiz/?list_only=true

#### Get just the IDs and titles of all quizzes

&lt;base-uri&gt;/quiz/?list_only=true.

For instance:
http://bigoquiz.com/api/quiz/?list_only=true

### Get a specific quiz

&lt;base-uri&gt;/quiz/{quiz-id}.

http://bigoquiz.com/api/quiz/algorithms

### Get the sections for a quiz

&lt;base-uri&gt;/quiz/{quiz-id}/section

#### Get just the IDs and titles of a section

&lt;base-uri&gt;/quiz/{quiz-id}/section?list-only=true

For instance:
http://bigoquiz.com/api/quiz/bigo/section?list-only=true

### Get a specific quiz question

&lt;base-uri&gt;/quiz/{quiz-id}/question/{question-id}

For instance:
http://bigoquiz.com/api/quiz/bigo/question/avl-tree-search-average

## Question

### Get the next question for a quiz

&lt;base-uri&gt;/question/next/quiz-id={quiz-id}

For instance:
http://bigoquiz.com/api/question/next?quiz-id=bigo

### Get the next question for a specific section of a quiz

&lt;base-uri&gt;/question/next/quiz-id={quiz-id}?section-id={section-id}

For instance:
http://bigoquiz.com/api/question/next?quiz-id=bigo?section-id=data-structure-operations

Example JSON Response. TODO.

### Get a specific question

&lt;base-uri&gt;/question/next/quiz-id={quiz-id}?section-id={section-id}

See also [Get a specific quiz question](#get-a-specific-quiz-question).

## User

### To get details about the logged-in user

&lt;base-uri&gt;/user

For instance:
http://bigoquiz.com/api/user

## User History

### To get the history for the logged-in user

&lt;base-uri&gt;/user-history

For instance:
http://bigoquiz.com/api/user-history

### To get the history for the logged-in user for a specific quiz

&lt;base-uri&gt;/user-history/{quiz-id}

For instance:
http://bigoquiz.com/api/user-history/distributed_systems


