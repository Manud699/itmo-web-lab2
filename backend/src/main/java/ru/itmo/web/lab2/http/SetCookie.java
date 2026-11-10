package ru.itmo.web.lab2.http;

/** One Set-Cookie header with its attributes. maxAgeSeconds = 0 tells the browser to delete the cookie. */
public record SetCookie(String name, String value, long maxAgeSeconds, String path,
                        boolean httpOnly, boolean secure, String sameSite) {

    public String toHeader() {
        StringBuilder sb = new StringBuilder(name).append('=').append(value)
                .append("; Max-Age=").append(maxAgeSeconds)
                .append("; Path=").append(path);
        if (httpOnly) sb.append("; HttpOnly");
        if (secure) sb.append("; Secure");
        if (sameSite != null) sb.append("; SameSite=").append(sameSite);
        return sb.toString();
    }
}
