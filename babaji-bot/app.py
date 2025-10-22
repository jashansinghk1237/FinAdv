# Zaroori libraries import karein
from flask import Flask, request, jsonify
from flask_cors import CORS
import google.generativeai as genai

# Flask app initialize karein
app = Flask(__name__)
# CORS enable karein taaki aapka webpage is server se baat kar sake
CORS(app)

# --- GEMINI AI CONFIGURATION ---
try:
    # NOTE: Apni API key yahan daalein
    genai.configure(api_key="AIzaSyCIuGE3mMS8YjKF_MIzIg7k3M-bIm3beyw")
    model = genai.GenerativeModel("gemini-2.5-flash-preview-05-20")
except Exception as e:
    print(f"Error configuring Gemini: {e}")
    model = None

# --- BABA-JI KA IMPROVED PROMPT ---
# Maine aapke prompt ko aur behtar bana diya hai
system_prompt = """
Aap "Baba-ji" hain, ek anubhavi aur shaant AI sant jo financial mamlon mein salah dete hain.
Aapka lakshya user ko saral, spasht, aur actionable salah dena hai.

Niyam:
1.  Hamesha "Beta," se shuru karein aur ek aashirvaad ya adhyatmik vichar (spiritual quote) ke saath samapt karein.
2.  Jawaab hamesha Hinglish (jaise WhatsApp par likhte hain) mein hona chahiye.
3.  Jatil vishayon ko hamesha point-by-point list mein samjhayein.
4.  User ke sawaal ko dhyan se samjhein aur seedha usi ka jawab dein.
---
User ka sawaal: 
"""

# --- API ENDPOINT ---
# Yeh woh URL hai jise aapka webpage call karega
@app.route('/ask-babaji', methods=['POST'])
def ask_babaji():
    if model is None:
        return jsonify({"error": "Gemini model is not configured correctly."}), 500

    user_input = request.json.get('prompt')
    if not user_input:
        return jsonify({"error": "Beta, aapne koi sawaal nahi poocha."}), 400

    try:
        # User ke input ko humare system prompt ke saath jod dein
        full_prompt = system_prompt + user_input
        
        # AI se response generate karwayein
        response = model.generate_content(full_prompt)
        
        # Response ko aasan text mein convert karke bhejein
        return jsonify({"response": response.text})
    except Exception as e:
        print(f"Error during generation: {e}")
        return jsonify({"error": "Beta, abhi kuch takniki samasya aa gayi hai."}), 500

# Server ko chalane ke liye
if __name__ == '__main__':
    app.run(port=5000)
