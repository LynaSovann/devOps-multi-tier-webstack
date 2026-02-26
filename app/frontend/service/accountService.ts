export const getAccountInfor = async (token: string) => {
  console.log("token ", token);
  console.log("Backend URL: ", process.env.NEXT_PUBLIC_BACKEND_URL);
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/account-info`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
      cache: "no-store",
    },
  );
  const data = await res.json();
  console.log("Account info: ", data);
  return data;
};
