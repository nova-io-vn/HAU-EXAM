export function mergeMessageById(messages, incoming) {
  if (!incoming?.id) return messages
  const index = messages.findIndex(message => message.id === incoming.id)
  if (index < 0) return [...messages, incoming]
  const next = [...messages]
  next[index] = { ...messages[index], ...incoming }
  return next
}

export function uniqueMessages(messages) {
  return messages.reduce(mergeMessageById, [])
}

export function hasConversationMessage(conversation) {
  return Boolean(conversation?.lastMessage && conversation.lastMessage !== 'Chưa có tin nhắn')
}

export function shouldSendOnKeyDown(event) {
  return event.key === 'Enter' && !event.shiftKey && !event.isComposing
}
