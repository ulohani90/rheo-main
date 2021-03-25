package com.rheotv.android.ui.activities.profile.viewprofile.utils

import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.data.network.models.postlisting.responses.PostGift
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.StreamHandler
import com.rheotv.android.utils.TimeUtils

open class StreamMessageHandler constructor(private val onEmit: (comment: CommentChat) -> Unit) : StreamHandler<CommentChat>() {

    override fun publish() {
        if (isQueueEmpty) {
            return
        }
        val queueSize = queue.size
        var pollTime = (20 / queueSize).toFloat()
        pollTime = if (pollTime >= 1) 1f else pollTime.toDouble().coerceAtLeast(0.2).toFloat()
        val commentChat = queue.poll()
        onEmit.invoke(commentChat)
        eventHandler.postDelayed(eventRunner, (pollTime * 1000).toLong())
    }
}

class CommentPublisher(onEmit: (comment: CommentChat) -> Unit) : StreamMessageHandler(onEmit)

open class MessageHandler<T> constructor(private val onEmit: (a: T) -> Unit) : StreamHandler<T>() {

    override fun publish() {
        if (isQueueEmpty) {
            return
        }
        val queueSize = queue.size
        var pollTime = (20 / queueSize).toFloat()
        pollTime = if (pollTime >= 1) 1f else pollTime.toDouble().coerceAtLeast(0.2).toFloat()
        val item = queue.poll()
        onEmit.invoke(item)
        eventHandler.postDelayed(eventRunner, (pollTime * 1000).toLong())
    }
}

class DynamicEventPublisher<StreamEventResponse>(onEmit: (response: StreamEventResponse) -> Unit) : MessageHandler<StreamEventResponse>(onEmit)

open class PostGiftMessageHandler constructor(private val onEmit: (greeting: PostGift?) -> Unit) : StreamHandler<PostGift>() {
    private var colors = arrayOf("#10945f", "#945010", "#c8931f", "#9021ff", "#cf1d75", "#2178ff")

    override fun publish() {
        if (isQueueEmpty) {
            onEmit.invoke(null)
            return
        }

        val position = CommonUtils.getRandomNumberInRange(0, colors.size - 1)
        val postGift = queue.poll()
        postGift.backgroundTintColor = colors[position]
        onEmit.invoke(postGift)
        eventHandler.postDelayed(eventRunner, TimeUtils.getTimeDiffInMs(postGift.startTimeTs, postGift.endTimeTs))
    }
}

class GreetingPublisher(onEmit: (greeting: PostGift) -> Unit) : PostGiftMessageHandler(onEmit as (PostGift?) -> Unit)

sealed class CommentAction {

    abstract val path: String

    data class Block(var username: String? = null) : CommentAction() {
        override val path: String
            get() = "block-user"

        override fun toString() = "block"
    }

    object Report : CommentAction() {
        override fun toString() = "report"
        override val path: String
            get() = "report-comment"
    }

    object Delete : CommentAction() {
        override fun toString() = "delete"
        override val path: String
            get() = "report-comment"
    }

    object Connect : CommentAction() {
        override val path: String
            get() = "connect"
    }
}

sealed class UserAction {
    object Delete: UserAction() {
        override fun toString() = "delete"
    }

    object Add: UserAction() {
        override fun toString() = "add"
    }

    object SignedUrl: UserAction() {
        override fun toString() = "signed-url"
    }

    object View: UserAction()
}