import { Injectable } from '@angular/core';

declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

/**
 * Uses the browser's built-in SpeechRecognition API. It cannot detect a
 * spoken language from audio - it must be told which language to listen
 * for before it starts. Rather than ask the passenger (a dropdown), this
 * defaults to the browser/OS locale (navigator.language) as an automatic
 * best guess. This is a real limitation, not a full fix: a kiosk set to
 * en-US will mis-transcribe a passenger speaking Arabic. See README for
 * the Whisper-based stretch goal that solves this properly by detecting
 * language from the audio itself.
 */
@Injectable({ providedIn: 'root' })
export class VoiceService {
  private recognition: any;
  private synth = window.speechSynthesis;

  constructor() {
    const SpeechRecognitionCtor = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognitionCtor) {
      this.recognition = new SpeechRecognitionCtor();
      this.recognition.continuous = false;
      this.recognition.interimResults = false;
    }
  }

  isSupported(): boolean {
    return !!this.recognition;
  }

  /** Automatically uses the browser/OS locale - no language picker needed. */
  listen(): Promise<string> {
    return new Promise((resolve, reject) => {
      if (!this.recognition) {
        reject('Speech recognition not supported in this browser');
        return;
      }
      this.recognition.lang = navigator.language || 'en-US';
      this.recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        resolve(transcript);
      };
      this.recognition.onerror = (event: any) => reject(event.error);
      this.recognition.start();
    });
  }

  /** Speaks text aloud in the given language code, e.g. 'hi', 'es', 'en'. */
  speak(text: string, langCode: string): void {
    if (!this.synth) return;
    this.synth.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = this.mapToBcp47(langCode);
    this.synth.speak(utterance);
  }

  private mapToBcp47(langCode: string): string {
    const map: Record<string, string> = {
      en: 'en-US', hi: 'hi-IN', es: 'es-ES', fr: 'fr-FR',
      ar: 'ar-SA', ja: 'ja-JP', zh: 'zh-CN', de: 'de-DE', pt: 'pt-BR'
    };
    return map[langCode] || 'en-US';
  }
}
