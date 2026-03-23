MODULE    : realtime
MUC DICH  : WebSocket - cap nhat so do mat bang real-time cho tat ca client dang xem
            Khi Space doi trang thai -> moi trinh duyet tu dong cap nhat (< 2 giay SOW)

FILES CAN TAO
=============

WebSocketConfig.java
  - registerStompEndpoints("/ws")
      FE ket noi vao endpoint nay: new SockJS("/ws")
      withSockJS() de fallback neu WebSocket khong ho tro

  - enableSimpleBroker("/topic")
      Kenh broadcast: /topic/spaces/{branchId}

  - setApplicationDestinationPrefixes("/app")
      FE gui len: /app/... (neu can 2 chieu)

SpaceStatusHandler.java
  - broadcastSpaceStatus(SpaceStatusDto dto)
      messagingTemplate.convertAndSend("/topic/spaces/" + dto.getBranchId(), dto)
      Duoc goi boi SpaceService moi khi co thay doi trang thai

CACH FE KET NOI (de thong bao cho Frontend dev):
  const socket = new SockJS("/ws");
  const stomp = Stomp.over(socket);
  stomp.connect({}, () => {
    stomp.subscribe("/topic/spaces/1", (msg) => {
      const data = JSON.parse(msg.body);
      // Cap nhat so do: data.spaceId, data.newStatus
    });
  });
