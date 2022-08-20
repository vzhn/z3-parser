# z3-parser
This is implementation of context-free grammars parser with z3 SMT solver.
Mostly it is proof of concept, it isn't rich and fast, but it is implemented without direct parser programming. Bunch of logical constraints on integer variables and SMT solver doing the job.

## Usage
Specify grammar and input string in separate files (see samples folder). Then invoke program like this:

`z3-parser -g samples/lambda.g -i samples/lambda_02.txt --goal T`

Here are program arguments:
```
Options: 
    --grammar, -g -> Grammar file (always required) { String }
    --input, -i -> Input file (always required) { String }
    --limit, -l -> derivation limit { Int }
    --goal { String }
    --help, -h -> Usage info 
```

### Program output
```
== input ==
λx.xλy.y

== grammar ==
T = V | APP | ABST
V = 'x' | 'y' | 'a' | 'b' | 'c'
APP = T T
ABST = 'λ' V '.' T

== derivation #1 ==
'λx.xλy.y'                                   # V(3) V(7)
'λx.' V('x') 'λy.' V('y')                    # V(1) T(3) V(5) T(7)
'λ' V('x') '.' T('x') 'λ' V('y') '.' T('y')  # ABST(0:3) ABST(4:7)
ABST('λx.x') ABST('λy.y')                    # T(0) T(1)
T('λx.x') T('λy.y')                          # APP(0:1)
APP('λx.xλy.y')                              # T(0)
T('λx.xλy.y')

== derivation #2 ==
'λx.xλy.y'                 # V(7)
'λx.xλy.' V('y')           # V(5) T(7)
'λx.xλ' V('y') '.' T('y')  # V(3) ABST(4:7)
'λx.' V('x') ABST('λy.y')  # T(3) T(4)
'λx.' T('x') T('λy.y')     # APP(3:4)
'λx.' APP('xλy.y')         # V(1) T(3)
'λ' V('x') '.' T('xλy.y')  # ABST(0:3)
ABST('λx.xλy.y')           # T(0)
T('λx.xλy.y')
```

Note that there are multiple derivations when grammar is ambiguous. 

## Setup
You need to build z3 and install **jar** to local repository, like this:

```
mvn install:install-file -Dfile=/home/vzhilin/.local/opt/z3-4.8.17-x64-glibc-2.31/bin/com.microsoft.z3.jar -DgroupId=com.microsoft -DartifactId=z3 -Dversion=4.8.17 -Dpackaging=jar
```

## How to launch program

### With IDEA
There are my IntelliJ IDEA Settings. Update them according your working environment.
```
Main class: me.vzhilin.gr.MainKt
VM Options: -Djava.library.path=/home/vzhilin/.local/opt/z3-z3-4.8.17/build
Program arguments: -g samples/lambda.g -i samples/lambda_02.txt --goal T
Working directory: /home/vzhilin/.local/Programming/Initiative/z3-parser
Environment variables: LD_LIBRARY_PATH=/home/vzhilin/.local/opt/z3-z3-4.8.17/build
```

### Without IDEA
At first, build the distribution:
```
./gradlew clean installDist
```

Then change directory and invoke program like this:
```
cd build/install/z3-parser

LD_LIBRARY_PATH=/home/vzhilin/.local/opt/z3-z3-4.8.17/build JAVA_OPTS=-Djava.library.path=/home/vzhilin/.local/opt/z3-z3-4.8.17/build bin/z3-parser -g samples/lambda.g -i samples/lambda_02.txt --goal T
```
