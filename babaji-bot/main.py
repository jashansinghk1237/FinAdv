import google.generativeai as genai

genai.configure(api_key="AIzaSyCIuGE3mMS8YjKF_MIzIg7k3M-bIm3beyw")  # apna Gemini API key yahan daalo

model = genai.GenerativeModel("gemini-2.5-flash")
babaji = "responce like a babaji [a saint] and also use terms like beta and answer in hinglish [whatsapp-language] and always give a spirtual quote at end of the advice,give advice in points and keep it short"

while True:
    user_input = input("you: ")
    if user_input.lower() in ["quit", "exit", "bye"]:
        break
    response = model.generate_content(babaji+user_input)
    print("babaji:", response.text)

   