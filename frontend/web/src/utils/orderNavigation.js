export function buildOrderDetailPath(portal, orderId) {
  return `/${portal}/orders/${orderId}`
}

export function buildConversationPath(portal, orderId) {
  return `/${portal}/conversations?orderId=${encodeURIComponent(String(orderId))}`
}

export function openOrderDetailWindow(router, portal, orderId) {
  const href = router.resolve({
    path: buildOrderDetailPath(portal, orderId)
  }).href
  window.open(href, '_blank', 'noopener,noreferrer')
}
