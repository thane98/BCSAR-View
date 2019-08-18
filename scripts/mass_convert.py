import os, sys, subprocess
from pathlib import Path

if os.name == 'nt':
    EXTENSION = ".exe"
else:
    EXTENSION = ""

ctrtool = os.getcwd() + "/ctrtool" + EXTENSION
if not os.path.isfile(ctrtool):
    print("Cannot find ctrtool.")
    exit(1)
    
target_dir = Path(sys.argv[1])

print("-----------------------------------------")
print("Beginning mass CWAV to WAV conversion.")
print("-----------------------------------------")
for f in target_dir.glob("**/*.cwav"):
    base = os.path.basename(f)
    dirname = os.path.dirname(f)
    filename = dirname + "/" + os.path.splitext(base)[0] + ".wav"
    print(filename)
    result = subprocess.call(["./ctrtool", "--wav", filename, f])
    if result != 0:
        print("Conversion error. Exiting...")
        exit(1)
    
