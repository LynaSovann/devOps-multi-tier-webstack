import { getServerSession } from "next-auth";
import { authOptions } from "../api/auth/[...nextauth]/route";
import { redirect } from "next/navigation";
import { getAccountInfor } from "@/service/accountService";

export default async function DashboardPage() {
  const session = await getServerSession(authOptions);

  if (!session?.payload?.token) {
    redirect("/login");
  }

  const { payload } = await getAccountInfor(session.payload.token);

  return (
    <div className="flex-1 overflow-auto">
      <div className="p-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-foreground mb-2">
            Welcome back, {`${payload.user.email}`}! 👋
          </h1>
          <p className="text-muted-foreground">
            Here's an overview of your workspace
          </p>
        </div>
      </div>
    </div>
  );
}
