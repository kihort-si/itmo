package ru.itmo.blps.app.security.access;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import ru.itmo.blps.app.models.enums.UserRole;

public final class RolePrivileges {
    private static final Map<UserRole, List<Privilege>> ROLE_PRIVILEGES = new EnumMap<>(UserRole.class);

    static {
        ROLE_PRIVILEGES.put(UserRole.CUSTOMER, List.of(
                Privilege.CATALOG_READ,
                Privilege.CART_MANAGE,
                Privilege.CUSTOMER_PROFILE_READ,
                Privilege.ORDER_CHECKOUT,
                Privilege.ORDER_READ_OWN
        ));
        ROLE_PRIVILEGES.put(UserRole.SHOP_ASSISTANT, List.of(
                Privilege.CATALOG_READ,
                Privilege.PRODUCT_MANAGE,
                Privilege.SHOP_MANAGE,
                Privilege.PROMO_MANAGE,
                Privilege.ORDER_READ_ASSIGNED,
                Privilege.ORDER_PROCESS_PICKUP,
                Privilege.ORDER_PROCESS_DELIVERY
        ));
        ROLE_PRIVILEGES.put(UserRole.COURIER, List.of(
                Privilege.ORDER_READ_ASSIGNED,
                Privilege.ORDER_PROCESS_DELIVERY
        ));
    }

    private RolePrivileges() {
    }

    public static List<Privilege> get(UserRole role) {
        return ROLE_PRIVILEGES.getOrDefault(role, List.of());
    }
}
