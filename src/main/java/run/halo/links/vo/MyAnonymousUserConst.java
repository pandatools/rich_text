package run.halo.links.vo;

public interface MyAnonymousUserConst {
    String PRINCIPAL = "anonymousUser";

    String Role = "anonymous";

    static boolean isAnonymousUser(String principal) {
        return PRINCIPAL.equals(principal);
    }
}
