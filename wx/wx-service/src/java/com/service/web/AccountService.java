package com.service.web;

import com.models.web.AccountInfo;
import com.models.web.AccountSelectInfo;
import com.models.web.SaveAccount;
import com.models.web.UserInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 2016/4/10.
 */
public interface AccountService {
    Map<String, Object> getAccountList(int pageIndex, int pageSize, String args);

    Map<String, Object> getAccountHtml(int pageIndex, int pageSize, String args);

    Map<String, Object> addAccount(SaveAccount model, UserInfo user);

    Map<String, Object> deleteAccount(List<Integer> list);

    SaveAccount getAccountById(int id);

    AccountInfo getAccountInfo(int id);

    List<AccountSelectInfo> getAccountSelect(int domain);
}
