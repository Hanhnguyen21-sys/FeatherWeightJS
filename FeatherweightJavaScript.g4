grammar FeatherweightJavaScript;

@header { package edu.sjsu.fwjs.parser; }

// Reserved words
IF: 'if';
ELSE: 'else';
WHILE: 'while';
FUNCTION: 'function';
VAR: 'var';
PRINT: 'print';

// Literals
INT: [1-9][0-9]* | '0';
BOOL: 'true' | 'false';
NULL: 'null';

// operators
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
MOD: '%';
GTE: '>=';
LTE: '<=';
GT: '>';
LT: '<';
EQ: '==';

SEPARATOR: ';';
COMMA: ','; //use in list of arguments in function
ASSIGN: '=';
LPAREN: '(';
RPAREN: ')';

// The first character must be an alphabetic character or an underscore. Remaining characters must
// be alphabetic characters, numeric characters, or underscores.
ID: [a-zA-Z_][a-zA-Z_0-9]*;

// Whitespace and comments
NEWLINE: '\r'? '\n' -> skip;
LINE_COMMENT: '//' ~[\n\r]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t]+ -> skip; // ignore whitespace

// ***Parsing rules ***

/** The start rule */
prog: stat+;
/**
 print (5); => print leftPar expr rightPar ; while (cond) {body} => while '(' expr')' block
 */
// statements
stat:
	expr SEPARATOR								# bareExpr
	| IF LPAREN expr RPAREN block ELSE block	# ifThenElse
	| IF LPAREN expr RPAREN block				# ifThen
	| WHILE LPAREN expr RPAREN block			# whileStatement
	| PRINT LPAREN expr RPAREN SEPARATOR		# printStatement
	| SEPARATOR									# emptyStatement;

// Expressions
expr: assignment;
// ID ASSIGN expre -> x = 3 (no var)
assignment:
	VAR ID ASSIGN assignment	# varDeclaration
	| ID ASSIGN assignment		# varReference
	| compare					# operationExpr;

// Operations
compare: addSub ( (LT | LTE | GT | GTE | EQ) addSub)*;

addSub: mulDiv ( (ADD | SUB) mulDiv)*;

mulDiv: callExpr ( (MUL | DIV | MOD) callExpr)*;

// Functions 
callExpr: primary (LPAREN argsList? RPAREN)*;
//argument list can be empty -> argsList?
funcExpr: FUNCTION LPAREN parametersList? RPAREN block;
argsList: expr (COMMA expr)*; // use for function call
parametersList: ID (COMMA ID)*; //use for function declaration

primary: INT | BOOL | NULL | ID | LPAREN expr RPAREN | funcExpr;

block: '{' stat* '}' # fullBlock | stat # simpBlock;