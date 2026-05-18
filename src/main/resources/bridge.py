import sys
import joblib
import os
import json

# Get the directory where bridge.py is located
dir_path = os.path.dirname(os.path.realpath(__file__))

# Load models from the same folder
try:
    model = joblib.load(os.path.join(dir_path, 'task_model.pkl'))
    vectorizer = joblib.load(os.path.join(dir_path, 'vectorizer.pkl'))
except Exception as e:
    print(json.dumps({"error": str(e)}))
    sys.exit(1)

def main():
    if len(sys.argv) > 1:
        user_input = sys.argv[1]
        vec = vectorizer.transform([user_input])
        intent = model.predict(vec)[0]
        
        # This outputs a clean JSON for Java to grab
        result = {
            "intent": str(intent),
            "task": user_input
        }
        print(json.dumps(result))

if __name__ == "__main__":
    main()
