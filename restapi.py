"""REST API run script"""
from flask import Flask
#from subprocess import call

app = Flask(__name__)
VERSION = "0.0"

@app.route('/version')
def version():
    """Returns version"""
    return ("Version %s\n" % VERSION)

if __name__ == '__main__':
    app.debug = True
    app.run(host='0.0.0.0', port=5555)
