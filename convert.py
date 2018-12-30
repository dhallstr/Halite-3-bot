import sys
out = ''
for i in range(1, len(sys.argv)):
    if (' ' in sys.argv[i]):
        out += '"' + sys.argv[i] + '"'
    elif (sys.argv[i][0] == 'v'):
        out += '"java -classpath ..\\versions\\' + sys.argv[i] + ' MyBot"'
    elif (sys.argv[i] == 'curr'):
        out += '"java MyBot"'
    elif (sys.argv[i][0] == 'run'):
        continue  #this is to prevent infinite recursion from taking up all the CPU
    else:
        out += sys.argv[i]
    if (i != len(sys.argv) - 1):
        out += ' '
print(out)
