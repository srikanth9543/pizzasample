package pizzasample.com.pizzasample.model;

import java.util.List;
import java.util.Set;

/**
 * Model class for to hold Pizza toppings.
 * Created by skanth on 4/14/16.
 */
public class PizzaToppings {

    private Set<String> mToppingType;

    public Set<String> getToppingType() {
        return mToppingType;
    }

    public void setToppingType(Set<String> toppingType) {
        mToppingType = toppingType;
    }

}
