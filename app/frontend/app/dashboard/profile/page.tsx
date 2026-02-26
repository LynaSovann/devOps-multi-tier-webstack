import { getServerSession } from "next-auth";
import ProfileComponent from "./_components/ProfileComponent";
import { authOptions } from "@/app/api/auth/[...nextauth]/route";
import { redirect } from "next/navigation";
import { getAccountInfor } from "@/service/accountService";

export default async function ProfilePage() {
  const session = await getServerSession(authOptions);

  if (!session?.payload?.token) {
    redirect("/login");
  }

  const data = await getAccountInfor(session.payload.token);

  return (
    <div className="flex-1 overflow-auto">
      <div className="p-8">
        <h1 className="text-3xl font-bold text-foreground mb-2">
          Your Profile
        </h1>
        <p className="text-muted-foreground mb-8">
          Manage your account settings
        </p>

        <ProfileComponent data={payload} />
      </div>
    </div>
  );
}
