package webapp;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

class Utils {

    /**
     * Searches the list of cookies and returns the requested one
     *
     * @param cookies    The list of cookies to search
     * @param cookieName The cookie to look for
     * @return The cookie requested, null if not found or if cookies was null
     */
    static Cookie findCookie(Cookie[] cookies, String cookieName) {

        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) return cookie;
        }
        return null;
    }

    static void deleteCookie(Cookie cookie, HttpServletResponse response) {
        if (cookie != null) {
            cookie.setValue(null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    static String getHrefButton(String href, String id, String content) {
        return "<a href=\"" + href + "\"><button type=\"button\" id=\"" + id + "\" > " +
                content + " </button></a>";
    }

    static String getSubmitButton(String id, String content) {
        return "<button id=\"" + id + "\" type=\"submit\"> " + content + " </button>";
    }

    static String getInputField(String idAndName, String placeholder, boolean required) {
        return "<input id=\"" + idAndName + "\" name=\"" + idAndName +
                "\" type=\"text\" placeholder=\"" + placeholder +
                "\" " + (required ? "required " : " ") + "/>";
    }

    static String getPostForm(String id, String action) {
        return "<form id=\"" + id + "\" method=\"POST\" action=\"" + action + "\"/>";
    }

    static String getTextArea(String idAndName, String placeholder, int rows, int cols) {
        return "<textarea id=\"" + idAndName + "\" name=\"" + idAndName +
                "\" placeholder=\"" + placeholder + "\" rows=\"" +
                rows + "\" cols=\"" + cols + "\"></textarea>";
    }

    static String getSpan(String id, String content) {
        return "<span id=\"" + id + "\">" + content + "</span>";
    }
}
