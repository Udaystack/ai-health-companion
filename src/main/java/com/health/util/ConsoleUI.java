package com.health.util;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

public class ConsoleUI {

    public static void init() {
        AnsiConsole.systemInstall();
    }

    public static void shutdown() {
        AnsiConsole.systemUninstall();
    }

    public static void printBanner() {
        System.out.println(ansi().fg(CYAN).bold().a("""
                ╔══════════════════════════════════════════════════╗
                ║          🏥  AI Health Companion  🤖             ║
                ║      Track · Analyze · Improve your health       ║
                ╚══════════════════════════════════════════════════╝
                """).reset());
    }

    public static void printMenu() {
        System.out.println(ansi().fg(YELLOW).bold().a("\n──────── MAIN MENU ────────").reset());
        System.out.println("  " + ansi().fg(GREEN).a("1").reset() + "  →  Add health report");
        System.out.println("  " + ansi().fg(GREEN).a("2").reset() + "  →  View all reports");
        System.out.println("  " + ansi().fg(GREEN).a("3").reset() + "  →  Analyze health trends (AI)");
        System.out.println("  " + ansi().fg(GREEN).a("4").reset() + "  →  Get weekly goals (AI)");
        System.out.println("  " + ansi().fg(GREEN).a("5").reset() + "  →  Explain a report (AI)");
        System.out.println("  " + ansi().fg(GREEN).a("6").reset() + "  →  Ask a health question (AI)");
        System.out.println("  " + ansi().fg(GREEN).a("7").reset() + "  →  View / edit my profile");
        System.out.println("  " + ansi().fg(GREEN).a("8").reset() + "  →  Delete a report");
        System.out.println("  " + ansi().fg(RED).a("0").reset()   + "  →  Exit");
        System.out.print(ansi().fg(YELLOW).a("\nYour choice: ").reset());
    }

    public static void printSuccess(String msg) {
        System.out.println(ansi().fg(GREEN).a("✔ " + msg).reset());
    }

    public static void printError(String msg) {
        System.out.println(ansi().fg(RED).a("✘ " + msg).reset());
    }

    public static void printInfo(String msg) {
        System.out.println(ansi().fg(CYAN).a("ℹ " + msg).reset());
    }

    public static void printWarning(String msg) {
        System.out.println(ansi().fg(YELLOW).a("⚠ " + msg).reset());
    }

    public static void printHeader(String title) {
        System.out.println(ansi().fg(MAGENTA).bold().a("\n══ " + title + " ══").reset());
    }

    public static void printAIResponse(String response) {
        System.out.println(ansi().fg(CYAN).bold().a("\n🤖 AI Analysis:\n").reset());
        System.out.println(ansi().fg(WHITE).a(response).reset());
        System.out.println(ansi().fg(CYAN).a("─".repeat(55)).reset());
    }

    public static void printReportRow(String id, String type, String date, int metricCount) {
        System.out.printf("  %s%-8s%s %-15s %-12s (%d metrics)%n",
                ansi().fg(YELLOW).toString(), id, ansi().reset().toString(),
                type, date, metricCount);
    }
}
