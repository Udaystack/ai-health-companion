# 🏥 AI Health Companion

A command-line Java application that tracks your health reports over time and uses **Claude AI** to analyze trends, explain medical results in plain language, and give personalized lifestyle recommendations.

---

## ✨ Features

| Feature | Description |
|---|---|
| 📋 **Health Report Tracking** | Store blood work, vitals, symptoms, and more with full metrics |
| 🤖 **AI Trend Analysis** | Claude AI analyzes all your reports to spot patterns and risks |
| 💡 **Personalized Suggestions** | Evidence-based lifestyle recommendations tailored to your data |
| 🎯 **Weekly Goals** | AI-generated weekly health goals based on your recent reports |
| 🔍 **Plain-language Explanations** | Understand any report without medical jargon |
| ❓ **Health Q&A** | Ask Claude any health question with your data as context |
| 💾 **Local JSON Storage** | All data stored locally at `~/.health-companion/` — your data stays private |

---

## 🚀 Getting Started

### Prerequisites


- Java 17+
- Maven 3.8+
- [Anthropic API key](https://console.anthropic.com/) (for AI features)

### Build

```bash
git clone https://github.com/YOUR_USERNAME/ai-health-companion.git
cd ai-health-companion
mvn clean package -DskipTests
```

### Run

```bash
# Set your API key
export ANTHROPIC_API_KEY=sk-ant-...

# Run the app
java -jar target/ai-health-companion-jar-with-dependencies.jar
```

---

## 🖥️ Usage

```
╔══════════════════════════════════════════════════╗
║          🏥  AI Health Companion  🤖             ║
║      Track · Analyze · Improve your health       ║
╚══════════════════════════════════════════════════╝

──────── MAIN MENU ────────
  1  →  Add health report
  2  →  View all reports
  3  →  Analyze health trends (AI)
  4  →  Get weekly goals (AI)
  5  →  Explain a report (AI)
  6  →  Ask a health question (AI)
  7  →  View / edit my profile
  8  →  Delete a report
  0  →  Exit
```

### Adding a Report

```
Report type: BLOOD_WORK
Date (yyyy-MM-dd) [today]:
Doctor name: Dr. Smith

Enter metrics (e.g. 'Blood Pressure: 120/80'). Empty line to finish.
  Metric: Hemoglobin: 13.5 g/dL
  Metric: WBC: 7.2 x10³/µL
  Metric: Blood Pressure: 118/76
  Metric:

Additional notes: Annual checkup, fasting sample
✔ Report saved with ID: A3F2B1C4
```

---

## 🗂️ Project Structure

```
src/
├── main/java/com/health/
│   ├── cli/
│   │   └── HealthCompanionApp.java     # Entry point & menu loop
│   ├── model/
│   │   ├── HealthReport.java           # Report data model
│   │   └── UserProfile.java            # User profile + BMI
│   ├── service/
│   │   ├── AIService.java              # Claude API integration
│   │   └── ReportService.java          # CRUD for reports
│   ├── storage/
│   │   └── StorageService.java         # JSON persistence
│   └── util/
│       └── ConsoleUI.java              # Colored terminal output
└── test/java/com/health/
    └── service/
        └── ReportServiceTest.java      # Unit tests
```

---

## 🔒 Privacy

All health data is stored **locally** on your machine at `~/.health-companion/`. Nothing is sent to external servers except the content you explicitly request analyzed via the Anthropic API.

---

## 🧪 Running Tests

```bash
mvn test
```

---

## 📝 Supported Report Types

- `BLOOD_WORK` — CBC, lipid panel, metabolic panel, etc.
- `VITALS` — blood pressure, heart rate, temperature, SpO2
- `SYMPTOMS` — track symptoms over time
- `DENTAL` — dental checkups and procedures
- `VISION` — eye exams and prescriptions
- `GENERAL` — general practitioner visits
- `OTHER` — anything else

---

## ⚠️ Disclaimer

This application is for **personal tracking and educational purposes only**. It is not a substitute for professional medical advice, diagnosis, or treatment. Always consult a qualified healthcare provider.

---

## 📄 License

MIT License — free to use, modify, and distribute.
