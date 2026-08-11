import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatResponse {
  detectedLanguage: string;
  languageName: string;
  intent: string;
  replyText: string;
  needsFollowup: boolean;
  followupQuestion: string;
  data: Record<string, any>;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly apiUrl = 'http://localhost:8080/api/chat';
  private sessionId = crypto.randomUUID();

  constructor(private http: HttpClient) {}

  send(message: string): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(this.apiUrl, {
      sessionId: this.sessionId,
      message
    });
  }

  transcribeAudio(blob: Blob): Observable<{ text: string }> {
    const formData = new FormData();
    formData.append('audio', blob, 'recording.webm');
    return this.http.post<{ text: string }>('http://localhost:8080/api/transcribe', formData);
  }
}
