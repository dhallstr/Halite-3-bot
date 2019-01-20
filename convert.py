import sys
def RepresentsInt(s):
    try: 
        int(s)
        return True
    except ValueError:
        return False
out = ''
for i in range(1, len(sys.argv)):
    if (' ' in sys.argv[i]):
        out += '"' + sys.argv[i] + '"'
    elif (sys.argv[i][0] == 'v'):
        out += '"java -classpath ..\\versions\\' + sys.argv[i] + ' MyBot"'
    elif (sys.argv[i] == 'curr'):
        out += '"java MyBot"'
    elif (RepresentsInt(sys.argv[i]) and not (i != 0 and sys.argv[i-1] == '-s')):
          out += '--width ' + sys.argv[i] + ' --height ' + sys.argv[i]
    elif (sys.argv[i][0] == 'run'):
        continue  #this is to prevent infinite recursion from taking up all the CPU
    else:
        out += sys.argv[i]
    if (i != len(sys.argv) - 1):
        out += ' '
print(out)
