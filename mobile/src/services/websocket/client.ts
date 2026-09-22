import { Client } from '@stomp/stompjs';
import { WS_BASE_URL } from '@/src/config/env';
import { getAccessToken } from '@/src/services/api';

const disconnectors = new Set<() => void>();
export function disconnectGatewaySocket() { disconnectors.forEach(disconnect => disconnect()); disconnectors.clear(); }

export function connectGatewaySocket({ onMessage, onStatus }: { onMessage: (message: unknown) => void; onStatus?: (status: 'connecting' | 'connected' | 'disconnected') => void }) {
  if (!WS_BASE_URL) return () => undefined;
  const client = new Client({ brokerURL: `${WS_BASE_URL}/ws`, reconnectDelay: 5000, connectionTimeout: 10000, heartbeatIncoming: 10000, heartbeatOutgoing: 10000, connectHeaders: {} });
  client.beforeConnect = async () => { const token = getAccessToken(); client.connectHeaders = token ? { Authorization: `Bearer ${token}` } : {}; onStatus?.('connecting'); };
  client.onConnect = () => { onStatus?.('connected'); client.subscribe('/user/queue/notifications', message => { try { onMessage(JSON.parse(message.body)); } catch { /* ignore malformed realtime payload */ } }); };
  client.onWebSocketClose = () => onStatus?.('disconnected');
  client.onStompError = () => onStatus?.('disconnected');
  client.activate();
  const disconnect = () => { void client.deactivate(); };
  disconnectors.add(disconnect);
  return () => { disconnect(); disconnectors.delete(disconnect); };
}
