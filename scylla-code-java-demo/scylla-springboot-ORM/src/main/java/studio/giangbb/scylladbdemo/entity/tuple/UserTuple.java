package studio.giangbb.scylladbdemo.entity.tuple;

import studio.giangbb.scylladbdemo.entity.udt.UserFavoritePlace;
import com.giangbb.scylla.core.mapping.Element;
import com.giangbb.scylla.core.mapping.Tuple;

/**
 * Created by Giangbb on 15/04/2024
 */

@Tuple
public class UserTuple {

    @Element(0)
    private String nickName;
    @Element(1)
    private int balance;
    @Element(2)
    private UserFavoritePlace userFavoritePlace;

    public UserTuple() {
    }

    public UserTuple(String nickName, int balance, UserFavoritePlace favoritePlace) {
        this.nickName = nickName;
        this.balance = balance;
        this.userFavoritePlace = favoritePlace;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public UserFavoritePlace getUserFavoritePlace() {
        return userFavoritePlace;
    }

    public void setUserFavoritePlace(UserFavoritePlace userFavoritePlace) {
        this.userFavoritePlace = userFavoritePlace;
    }

    @Override
    public String toString() {
        return "UserTuple{" +
                "nickName='" + nickName + '\'' +
                ", balance=" + balance +
                ", favoritePlace=" + userFavoritePlace +
                '}';
    }
}
