package com.rheotv.android.ui.activities.chatActivity;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.postlisting.responses.support.ChatModel;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

public class ChatFragmentViewModel extends BaseViewModel<ChatFragmentNavigator> {

    public final ObservableList<ChatModel> chatList = new ObservableArrayList<>();

    private final MutableLiveData<List<ChatModel>> chatListLiveData;

    private int offset = 0;

    public ChatFragmentViewModel(DataManager dataManager,
                                 SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        chatListLiveData = new MutableLiveData<>();
        offset = 0;
    }

    public void addBlogItemsToList(List<ChatModel> blogs) {
        if (offset == 0) {
            chatList.clear();
        }
        chatList.addAll(blogs);
    }

    public void addItemInFront(ChatModel result) {
        if (chatListLiveData.getValue() != null) {
            chatList.add(result);
            chatListLiveData.getValue().add(result);
        } else {
            ArrayList<ChatModel> ChatModels = new ArrayList<>();
            ChatModels.add(result);
            chatList.add(result);
            chatListLiveData.postValue(ChatModels);
        }
    }

    public int getOffset() {
        return offset;
    }

    public void fetchChatDetails(int offset, String userName) {
        setIsLoading(true);
        this.offset = offset;
        getCompositeDisposable().add(getDataManager()
                .getChatDetails(offset, userName)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    if (blogResponse != null && blogResponse.getCommentChatList() != null) {
                        if (blogResponse.getCommentChatList().size() > 0) {
                            chatListLiveData.setValue(blogResponse.getCommentChatList());
                        }
                    }
                    setIsLoading(false);
                }, throwable -> {
                    setIsLoading(false);
                    getNavigator().onError(throwable);
                }));
    }

    public void postChatMessage(String message, String userName) {
        setIsLoading(true);
        this.offset = offset;
        getCompositeDisposable().add(getDataManager()
                .postChat(message, userName)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    if (blogResponse != null && blogResponse.getCommentChatList() != null) {
                        if (blogResponse.getCommentChatList().size() > 0) {
                            addItemInFront(blogResponse.getCommentChatList().get(0));
                        }
                    }
                    setIsLoading(false);
                }, throwable -> {
                    setIsLoading(false);
                    getNavigator().onError(throwable);
                }));
    }


    public MutableLiveData<List<ChatModel>> getBlogListLiveData() {
        return chatListLiveData;
    }

    public ObservableList<ChatModel> getBlogObservableList() {
        return chatList;
    }
}
