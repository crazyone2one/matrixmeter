const ACCESS_TOKEN = "accessToken";
const REFRESHTOKEN = "refreshToken";
const hasToken = (name?: string) => {
  // if (WHITE_LIST_NAME.includes(name)) {
  //   return true;
  // }
  return (
    !!localStorage.getItem(ACCESS_TOKEN) && !!localStorage.getItem(REFRESHTOKEN)
  );
};
const clearToken = () => {
  localStorage.removeItem(ACCESS_TOKEN);
  localStorage.removeItem(REFRESHTOKEN);
};
const getToken = () => {
  return {
    [ACCESS_TOKEN]: localStorage.getItem(ACCESS_TOKEN),
    [REFRESHTOKEN]: localStorage.getItem(REFRESHTOKEN) || "",
  };
};

const setToken = (sessionId: string, csrfToken: string) => {
  localStorage.setItem(ACCESS_TOKEN, sessionId);
  localStorage.setItem(REFRESHTOKEN, csrfToken);
};
export { clearToken, getToken, hasToken, setToken };
