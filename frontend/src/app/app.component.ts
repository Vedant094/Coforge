import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatResponse } from './services/chat.service';
import { VoiceService } from './services/voice.service';

interface Message {
  sender: 'user' | 'bot';
  text: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  messages: Message[] = [
    { sender: 'bot', text: 'Welcome! Tap the mic and speak, or type below. Try: "I need to check in, my PNR is ABC123".' }
  ];
  userInput = '';
  isListening = false;
  isLoading = false;
  lastData: Record<string, any> | null = null;
  lastIntent = '';

  constructor(private chatService: ChatService, private voiceService: VoiceService) {}

  get voiceSupported(): boolean {
    return this.voiceService.isSupported();
  }

  sendText(): void {
    const text = this.userInput.trim();
    if (!text) return;
    this.userInput = '';
    this.pushMessage('user', text);
    this.callBackend(text);
  }

  startListening(): void {
    if (!this.voiceSupported) return;
    this.isListening = true;
    this.voiceService.listen()
      .then(transcript => {
        this.isListening = false;
        this.pushMessage('user', transcript);
        this.callBackend(transcript);
      })
      .catch(() => {
        this.isListening = false;
      });
  }

  private callBackend(text: string): void {
    this.isLoading = true;
    this.chatService.send(text).subscribe({
      next: (res: ChatResponse) => {
        this.isLoading = false;
        const replyText = res.needsFollowup ? res.followupQuestion : res.replyText;
        this.pushMessage('bot', `${replyText} (${res.languageName})`);
        this.lastData = res.data;
        this.lastIntent = res.intent;
        this.voiceService.speak(replyText, res.detectedLanguage);
      },
      error: () => {
        this.isLoading = false;
        this.pushMessage('bot', 'Sorry, something went wrong reaching the assistant.');
      }
    });
  }

  private pushMessage(sender: 'user' | 'bot', text: string): void {
    this.messages.push({ sender, text });
  }
}
