import LoginComponent from "./_components/LoginComponent";

export default function LoginPage() {
  console.log("Backend URL: ", process.env.NEXT_PUBLIC_BACKEND_URL);
  console.log("NextAuth Secret: ", process.env.NEXTAUTH_URL);
  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-foreground mb-2">Welcome</h1>
          <p className="text-muted-foreground">Please login to get Access</p>
        </div>

        <LoginComponent />
      </div>
    </div>
  );
}
