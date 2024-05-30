package ru.itmo.client.builders;

import ru.itmo.client.utils.Input;
import ru.itmo.common.user.User;
import ru.itmo.common.utils.Console;

/**
 * @author Nikita Vasilev
 */
public class UserBuilder extends Builder<User> {
    private final Console console;

    public UserBuilder(Console console) {
        this.console = console;
    }

    @Override
    public User build() {
        return new User(
                Integer.MAX_VALUE,
                enterLogin(),
                enterPassword()
        );
    }

    private String enterLogin() {
        String login;
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите логин: ");
                }
                login = Input.getUserScanner().nextLine().trim();
                if (login.isEmpty()) throw new NullPointerException();
                else if (login.length() < 6 || login.length() > 50) throw new IllegalArgumentException();
                break;
            } catch (NullPointerException e) {
                console.printError("Логин не может быть null");
            } catch (IllegalArgumentException e) {
                console.printError("Логин не может быть меньше 6 или больше 50 символов");
            }
        }
        return login;
    }

    private String enterPassword() {
        String password;
        boolean fileMode = Input.isFileMode();
        while (true) {
            try {
                if (!fileMode) {
                    console.println("Введите пароль: ");
                }
                password = Input.getUserScanner().nextLine().trim();
                if (password.isEmpty()) throw new NullPointerException();
                else if (password.length() < 8 || password.length() > 64) throw new IllegalArgumentException();
                break;
            } catch (NullPointerException e) {
                console.printError("Пароль не может быть null");
            } catch (IllegalArgumentException e) {
                console.printError("Пароль не может быть меньше 8 или больше 64 символов");
            }
        }
        return password;
    }
}
