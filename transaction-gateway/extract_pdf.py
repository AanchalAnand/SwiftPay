import pdfplumber

try:
    with pdfplumber.open(r'D:\SwiftPay\src\main\utils\Java Developer Hackathon Challenge (1).pdf') as pdf:
        for i, page in enumerate(pdf.pages):
            text = page.extract_text()
            print(f"--- PAGE {i+1} ---\n{text}\n")
except Exception as e:
    print(f"Error: {e}")
