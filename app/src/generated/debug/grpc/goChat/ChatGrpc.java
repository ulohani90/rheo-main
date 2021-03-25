package goChat;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * Defines the service between client and server.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.21.0)",
    comments = "Source: services.proto")
public final class ChatGrpc {

  private ChatGrpc() {}

  public static final String SERVICE_NAME = "goChat.Chat";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<goChat.Services.ChatMessage,
      goChat.Services.ChatMessage> getRouteChatMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RouteChat",
      requestType = goChat.Services.ChatMessage.class,
      responseType = goChat.Services.ChatMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<goChat.Services.ChatMessage,
      goChat.Services.ChatMessage> getRouteChatMethod() {
    io.grpc.MethodDescriptor<goChat.Services.ChatMessage, goChat.Services.ChatMessage> getRouteChatMethod;
    if ((getRouteChatMethod = ChatGrpc.getRouteChatMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getRouteChatMethod = ChatGrpc.getRouteChatMethod) == null) {
          ChatGrpc.getRouteChatMethod = getRouteChatMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.ChatMessage, goChat.Services.ChatMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "RouteChat"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.ChatMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.ChatMessage.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getRouteChatMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.Empty> getLeaveRoomMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "LeaveRoom",
      requestType = goChat.Services.GroupInfo.class,
      responseType = goChat.Services.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.Empty> getLeaveRoomMethod() {
    io.grpc.MethodDescriptor<goChat.Services.GroupInfo, goChat.Services.Empty> getLeaveRoomMethod;
    if ((getLeaveRoomMethod = ChatGrpc.getLeaveRoomMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getLeaveRoomMethod = ChatGrpc.getLeaveRoomMethod) == null) {
          ChatGrpc.getLeaveRoomMethod = getLeaveRoomMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.GroupInfo, goChat.Services.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "LeaveRoom"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.GroupInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.Empty.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getLeaveRoomMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.ClientCount> getGetGroupClientCountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetGroupClientCount",
      requestType = goChat.Services.GroupInfo.class,
      responseType = goChat.Services.ClientCount.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.ClientCount> getGetGroupClientCountMethod() {
    io.grpc.MethodDescriptor<goChat.Services.GroupInfo, goChat.Services.ClientCount> getGetGroupClientCountMethod;
    if ((getGetGroupClientCountMethod = ChatGrpc.getGetGroupClientCountMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getGetGroupClientCountMethod = ChatGrpc.getGetGroupClientCountMethod) == null) {
          ChatGrpc.getGetGroupClientCountMethod = getGetGroupClientCountMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.GroupInfo, goChat.Services.ClientCount>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "GetGroupClientCount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.GroupInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.ClientCount.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetGroupClientCountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.ClientInfo,
      goChat.Services.Empty> getUnRegisterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UnRegister",
      requestType = goChat.Services.ClientInfo.class,
      responseType = goChat.Services.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.ClientInfo,
      goChat.Services.Empty> getUnRegisterMethod() {
    io.grpc.MethodDescriptor<goChat.Services.ClientInfo, goChat.Services.Empty> getUnRegisterMethod;
    if ((getUnRegisterMethod = ChatGrpc.getUnRegisterMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getUnRegisterMethod = ChatGrpc.getUnRegisterMethod) == null) {
          ChatGrpc.getUnRegisterMethod = getUnRegisterMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.ClientInfo, goChat.Services.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "UnRegister"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.ClientInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.Empty.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getUnRegisterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.ClientInfo,
      goChat.Services.Empty> getRegisterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Register",
      requestType = goChat.Services.ClientInfo.class,
      responseType = goChat.Services.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.ClientInfo,
      goChat.Services.Empty> getRegisterMethod() {
    io.grpc.MethodDescriptor<goChat.Services.ClientInfo, goChat.Services.Empty> getRegisterMethod;
    if ((getRegisterMethod = ChatGrpc.getRegisterMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getRegisterMethod = ChatGrpc.getRegisterMethod) == null) {
          ChatGrpc.getRegisterMethod = getRegisterMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.ClientInfo, goChat.Services.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "Register"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.ClientInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.Empty.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getRegisterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.Empty> getCreateGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateGroup",
      requestType = goChat.Services.GroupInfo.class,
      responseType = goChat.Services.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.Empty> getCreateGroupMethod() {
    io.grpc.MethodDescriptor<goChat.Services.GroupInfo, goChat.Services.Empty> getCreateGroupMethod;
    if ((getCreateGroupMethod = ChatGrpc.getCreateGroupMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getCreateGroupMethod = ChatGrpc.getCreateGroupMethod) == null) {
          ChatGrpc.getCreateGroupMethod = getCreateGroupMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.GroupInfo, goChat.Services.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "CreateGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.GroupInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.Empty.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getCreateGroupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.Empty> getJoinGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "JoinGroup",
      requestType = goChat.Services.GroupInfo.class,
      responseType = goChat.Services.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.Empty> getJoinGroupMethod() {
    io.grpc.MethodDescriptor<goChat.Services.GroupInfo, goChat.Services.Empty> getJoinGroupMethod;
    if ((getJoinGroupMethod = ChatGrpc.getJoinGroupMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getJoinGroupMethod = ChatGrpc.getJoinGroupMethod) == null) {
          ChatGrpc.getJoinGroupMethod = getJoinGroupMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.GroupInfo, goChat.Services.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "JoinGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.GroupInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.Empty.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getJoinGroupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.Empty,
      goChat.Services.GroupList> getGetGroupListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetGroupList",
      requestType = goChat.Services.Empty.class,
      responseType = goChat.Services.GroupList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.Empty,
      goChat.Services.GroupList> getGetGroupListMethod() {
    io.grpc.MethodDescriptor<goChat.Services.Empty, goChat.Services.GroupList> getGetGroupListMethod;
    if ((getGetGroupListMethod = ChatGrpc.getGetGroupListMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getGetGroupListMethod = ChatGrpc.getGetGroupListMethod) == null) {
          ChatGrpc.getGetGroupListMethod = getGetGroupListMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.Empty, goChat.Services.GroupList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "GetGroupList"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.GroupList.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetGroupListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.ClientList> getGetGroupClientListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetGroupClientList",
      requestType = goChat.Services.GroupInfo.class,
      responseType = goChat.Services.ClientList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.GroupInfo,
      goChat.Services.ClientList> getGetGroupClientListMethod() {
    io.grpc.MethodDescriptor<goChat.Services.GroupInfo, goChat.Services.ClientList> getGetGroupClientListMethod;
    if ((getGetGroupClientListMethod = ChatGrpc.getGetGroupClientListMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getGetGroupClientListMethod = ChatGrpc.getGetGroupClientListMethod) == null) {
          ChatGrpc.getGetGroupClientListMethod = getGetGroupClientListMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.GroupInfo, goChat.Services.ClientList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "GetGroupClientList"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.GroupInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.ClientList.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetGroupClientListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<goChat.Services.Empty,
      goChat.Services.ClientList> getGetClientListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetClientList",
      requestType = goChat.Services.Empty.class,
      responseType = goChat.Services.ClientList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<goChat.Services.Empty,
      goChat.Services.ClientList> getGetClientListMethod() {
    io.grpc.MethodDescriptor<goChat.Services.Empty, goChat.Services.ClientList> getGetClientListMethod;
    if ((getGetClientListMethod = ChatGrpc.getGetClientListMethod) == null) {
      synchronized (ChatGrpc.class) {
        if ((getGetClientListMethod = ChatGrpc.getGetClientListMethod) == null) {
          ChatGrpc.getGetClientListMethod = getGetClientListMethod = 
              io.grpc.MethodDescriptor.<goChat.Services.Empty, goChat.Services.ClientList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "goChat.Chat", "GetClientList"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  goChat.Services.ClientList.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getGetClientListMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ChatStub newStub(io.grpc.Channel channel) {
    return new ChatStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ChatBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ChatBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ChatFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ChatFutureStub(channel);
  }

  /**
   * <pre>
   * Defines the service between client and server.
   * </pre>
   */
  public static abstract class ChatImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<goChat.Services.ChatMessage> routeChat(
        io.grpc.stub.StreamObserver<goChat.Services.ChatMessage> responseObserver) {
      return asyncUnimplementedStreamingCall(getRouteChatMethod(), responseObserver);
    }

    /**
     */
    public void leaveRoom(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getLeaveRoomMethod(), responseObserver);
    }

    /**
     */
    public void getGroupClientCount(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.ClientCount> responseObserver) {
      asyncUnimplementedUnaryCall(getGetGroupClientCountMethod(), responseObserver);
    }

    /**
     */
    public void unRegister(goChat.Services.ClientInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getUnRegisterMethod(), responseObserver);
    }

    /**
     */
    public void register(goChat.Services.ClientInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getRegisterMethod(), responseObserver);
    }

    /**
     */
    public void createGroup(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateGroupMethod(), responseObserver);
    }

    /**
     */
    public void joinGroup(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getJoinGroupMethod(), responseObserver);
    }

    /**
     */
    public void getGroupList(goChat.Services.Empty request,
        io.grpc.stub.StreamObserver<goChat.Services.GroupList> responseObserver) {
      asyncUnimplementedUnaryCall(getGetGroupListMethod(), responseObserver);
    }

    /**
     */
    public void getGroupClientList(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.ClientList> responseObserver) {
      asyncUnimplementedUnaryCall(getGetGroupClientListMethod(), responseObserver);
    }

    /**
     */
    public void getClientList(goChat.Services.Empty request,
        io.grpc.stub.StreamObserver<goChat.Services.ClientList> responseObserver) {
      asyncUnimplementedUnaryCall(getGetClientListMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRouteChatMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                goChat.Services.ChatMessage,
                goChat.Services.ChatMessage>(
                  this, METHODID_ROUTE_CHAT)))
          .addMethod(
            getLeaveRoomMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.GroupInfo,
                goChat.Services.Empty>(
                  this, METHODID_LEAVE_ROOM)))
          .addMethod(
            getGetGroupClientCountMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.GroupInfo,
                goChat.Services.ClientCount>(
                  this, METHODID_GET_GROUP_CLIENT_COUNT)))
          .addMethod(
            getUnRegisterMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.ClientInfo,
                goChat.Services.Empty>(
                  this, METHODID_UN_REGISTER)))
          .addMethod(
            getRegisterMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.ClientInfo,
                goChat.Services.Empty>(
                  this, METHODID_REGISTER)))
          .addMethod(
            getCreateGroupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.GroupInfo,
                goChat.Services.Empty>(
                  this, METHODID_CREATE_GROUP)))
          .addMethod(
            getJoinGroupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.GroupInfo,
                goChat.Services.Empty>(
                  this, METHODID_JOIN_GROUP)))
          .addMethod(
            getGetGroupListMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.Empty,
                goChat.Services.GroupList>(
                  this, METHODID_GET_GROUP_LIST)))
          .addMethod(
            getGetGroupClientListMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.GroupInfo,
                goChat.Services.ClientList>(
                  this, METHODID_GET_GROUP_CLIENT_LIST)))
          .addMethod(
            getGetClientListMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                goChat.Services.Empty,
                goChat.Services.ClientList>(
                  this, METHODID_GET_CLIENT_LIST)))
          .build();
    }
  }

  /**
   * <pre>
   * Defines the service between client and server.
   * </pre>
   */
  public static final class ChatStub extends io.grpc.stub.AbstractStub<ChatStub> {
    private ChatStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChatStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChatStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChatStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<goChat.Services.ChatMessage> routeChat(
        io.grpc.stub.StreamObserver<goChat.Services.ChatMessage> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getRouteChatMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void leaveRoom(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getLeaveRoomMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getGroupClientCount(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.ClientCount> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetGroupClientCountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void unRegister(goChat.Services.ClientInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUnRegisterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void register(goChat.Services.ClientInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRegisterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createGroup(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void joinGroup(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getJoinGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getGroupList(goChat.Services.Empty request,
        io.grpc.stub.StreamObserver<goChat.Services.GroupList> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetGroupListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getGroupClientList(goChat.Services.GroupInfo request,
        io.grpc.stub.StreamObserver<goChat.Services.ClientList> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetGroupClientListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getClientList(goChat.Services.Empty request,
        io.grpc.stub.StreamObserver<goChat.Services.ClientList> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetClientListMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Defines the service between client and server.
   * </pre>
   */
  public static final class ChatBlockingStub extends io.grpc.stub.AbstractStub<ChatBlockingStub> {
    private ChatBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChatBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChatBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChatBlockingStub(channel, callOptions);
    }

    /**
     */
    public goChat.Services.Empty leaveRoom(goChat.Services.GroupInfo request) {
      return blockingUnaryCall(
          getChannel(), getLeaveRoomMethod(), getCallOptions(), request);
    }

    /**
     */
    public goChat.Services.ClientCount getGroupClientCount(goChat.Services.GroupInfo request) {
      return blockingUnaryCall(
          getChannel(), getGetGroupClientCountMethod(), getCallOptions(), request);
    }

    /**
     */
    public goChat.Services.Empty unRegister(goChat.Services.ClientInfo request) {
      return blockingUnaryCall(
          getChannel(), getUnRegisterMethod(), getCallOptions(), request);
    }

    /**
     */
    public goChat.Services.Empty register(goChat.Services.ClientInfo request) {
      return blockingUnaryCall(
          getChannel(), getRegisterMethod(), getCallOptions(), request);
    }

    /**
     */
    public goChat.Services.Empty createGroup(goChat.Services.GroupInfo request) {
      return blockingUnaryCall(
          getChannel(), getCreateGroupMethod(), getCallOptions(), request);
    }

    /**
     */
    public goChat.Services.Empty joinGroup(goChat.Services.GroupInfo request) {
      return blockingUnaryCall(
          getChannel(), getJoinGroupMethod(), getCallOptions(), request);
    }

    /**
     */
    public goChat.Services.GroupList getGroupList(goChat.Services.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetGroupListMethod(), getCallOptions(), request);
    }

    /**
     */
    public goChat.Services.ClientList getGroupClientList(goChat.Services.GroupInfo request) {
      return blockingUnaryCall(
          getChannel(), getGetGroupClientListMethod(), getCallOptions(), request);
    }

    /**
     */
    public goChat.Services.ClientList getClientList(goChat.Services.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetClientListMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Defines the service between client and server.
   * </pre>
   */
  public static final class ChatFutureStub extends io.grpc.stub.AbstractStub<ChatFutureStub> {
    private ChatFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChatFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChatFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChatFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.Empty> leaveRoom(
        goChat.Services.GroupInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getLeaveRoomMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.ClientCount> getGroupClientCount(
        goChat.Services.GroupInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getGetGroupClientCountMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.Empty> unRegister(
        goChat.Services.ClientInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getUnRegisterMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.Empty> register(
        goChat.Services.ClientInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getRegisterMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.Empty> createGroup(
        goChat.Services.GroupInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateGroupMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.Empty> joinGroup(
        goChat.Services.GroupInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getJoinGroupMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.GroupList> getGroupList(
        goChat.Services.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetGroupListMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.ClientList> getGroupClientList(
        goChat.Services.GroupInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getGetGroupClientListMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<goChat.Services.ClientList> getClientList(
        goChat.Services.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetClientListMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LEAVE_ROOM = 0;
  private static final int METHODID_GET_GROUP_CLIENT_COUNT = 1;
  private static final int METHODID_UN_REGISTER = 2;
  private static final int METHODID_REGISTER = 3;
  private static final int METHODID_CREATE_GROUP = 4;
  private static final int METHODID_JOIN_GROUP = 5;
  private static final int METHODID_GET_GROUP_LIST = 6;
  private static final int METHODID_GET_GROUP_CLIENT_LIST = 7;
  private static final int METHODID_GET_CLIENT_LIST = 8;
  private static final int METHODID_ROUTE_CHAT = 9;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ChatImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ChatImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LEAVE_ROOM:
          serviceImpl.leaveRoom((goChat.Services.GroupInfo) request,
              (io.grpc.stub.StreamObserver<goChat.Services.Empty>) responseObserver);
          break;
        case METHODID_GET_GROUP_CLIENT_COUNT:
          serviceImpl.getGroupClientCount((goChat.Services.GroupInfo) request,
              (io.grpc.stub.StreamObserver<goChat.Services.ClientCount>) responseObserver);
          break;
        case METHODID_UN_REGISTER:
          serviceImpl.unRegister((goChat.Services.ClientInfo) request,
              (io.grpc.stub.StreamObserver<goChat.Services.Empty>) responseObserver);
          break;
        case METHODID_REGISTER:
          serviceImpl.register((goChat.Services.ClientInfo) request,
              (io.grpc.stub.StreamObserver<goChat.Services.Empty>) responseObserver);
          break;
        case METHODID_CREATE_GROUP:
          serviceImpl.createGroup((goChat.Services.GroupInfo) request,
              (io.grpc.stub.StreamObserver<goChat.Services.Empty>) responseObserver);
          break;
        case METHODID_JOIN_GROUP:
          serviceImpl.joinGroup((goChat.Services.GroupInfo) request,
              (io.grpc.stub.StreamObserver<goChat.Services.Empty>) responseObserver);
          break;
        case METHODID_GET_GROUP_LIST:
          serviceImpl.getGroupList((goChat.Services.Empty) request,
              (io.grpc.stub.StreamObserver<goChat.Services.GroupList>) responseObserver);
          break;
        case METHODID_GET_GROUP_CLIENT_LIST:
          serviceImpl.getGroupClientList((goChat.Services.GroupInfo) request,
              (io.grpc.stub.StreamObserver<goChat.Services.ClientList>) responseObserver);
          break;
        case METHODID_GET_CLIENT_LIST:
          serviceImpl.getClientList((goChat.Services.Empty) request,
              (io.grpc.stub.StreamObserver<goChat.Services.ClientList>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ROUTE_CHAT:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.routeChat(
              (io.grpc.stub.StreamObserver<goChat.Services.ChatMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ChatGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getRouteChatMethod())
              .addMethod(getLeaveRoomMethod())
              .addMethod(getGetGroupClientCountMethod())
              .addMethod(getUnRegisterMethod())
              .addMethod(getRegisterMethod())
              .addMethod(getCreateGroupMethod())
              .addMethod(getJoinGroupMethod())
              .addMethod(getGetGroupListMethod())
              .addMethod(getGetGroupClientListMethod())
              .addMethod(getGetClientListMethod())
              .build();
        }
      }
    }
    return result;
  }
}
