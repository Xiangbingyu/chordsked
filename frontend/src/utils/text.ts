function looksMojibake(value: string) {
  return /[ÃÂÐÑåæçéèêëìíîïðñòóôõöùúûüýþÿ]/.test(value)
}

function decodeLatin1AsUtf8(value: string) {
  const bytes = Uint8Array.from(value, (char) => char.charCodeAt(0))
  return new TextDecoder('utf-8', { fatal: false }).decode(bytes)
}

export function normalizeChineseText(value: string) {
  if (!value || !looksMojibake(value)) {
    return value
  }
  try {
    const decoded = decodeLatin1AsUtf8(value)
    return decoded || value
  } catch {
    return value
  }
}

