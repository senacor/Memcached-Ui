import time
import string
import random
from pymemcache.client import base

# number of keys to generate
KEYS_NUMBER = 10000
# length of key names
KEY_LENGTH = 12
# length of values
VALUE_LENGTH = 100
#namespaces
NAMESPACES = [
    'namespace-a',
    'namespace-b',
    'namespace-c',
    'namespace-d',
    'namespace-f',
]
MIN_MILLIS = 1629000000000
MAX_MILLIS = int(time.time() * 1000)

# generate a random number of length n
def getRandomNumber(n):
    rangeStart = 10**(n-1)
    rangeEnd = (10**n)-1
    return random.randint(rangeStart, rangeEnd)

# generate a random string of length n
def getRandomString(n):
    letters = string.ascii_lowercase + '_' + '[' + ']'
    return ''.join(random.choice(letters) for i in range(n))

def getRandomTimestampInMillis():
    return random.randint(MIN_MILLIS, MAX_MILLIS)

# generates a specified number of random key-valu pairs.
def generateRandomEntries():
    entries = {}
    for i in range(KEYS_NUMBER):
        key = random.choice(NAMESPACES) + ':' + str(getRandomTimestampInMillis()) + ':' + getRandomString(KEY_LENGTH)
        value = str(getRandomNumber(VALUE_LENGTH))
        entries[key] = value
    return entries


if __name__ == '__main__':
    # generate random entries and store them in local memcached
    entries = generateRandomEntries()
    print('Example entry: %s' % list(entries.keys())[0])

    client = base.Client(('memcached', 11211))

    for key in entries:
        client.set(key, entries[key])
