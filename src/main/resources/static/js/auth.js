// ✅ Backend base URL
const backendUrl = "http://localhost:8080";

// =================== LOGIN ===================
const loginForm = document.getElementById("loginForm");
if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();
        const messageEl = document.getElementById("message");

        try {
            const response = await fetch(`${backendUrl}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            const data = await response.json();

            if (response.ok) {
                localStorage.setItem("token", data.token);
                messageEl.textContent = "✅ Login successful!";
                messageEl.style.color = "green";
                setTimeout(() => (window.location.href = "upload.html"), 1000);
            } else {
                messageEl.textContent = data.message || "❌ Login failed.";
                messageEl.style.color = "red";
            }
        } catch (err) {
            messageEl.textContent = "⚠️ Error connecting to server.";
            messageEl.style.color = "red";
            console.error(err);
        }
    });
}

// =================== SIGNUP ===================
const signupForm = document.getElementById("signupForm");
if (signupForm) {
    signupForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const firstName = document.getElementById("firstName").value.trim();
        const lastName = document.getElementById("lastName").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();
        const messageEl = document.getElementById("message");

        try {
            const response = await fetch(`${backendUrl}/auth/signup`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ firstName, lastName, email, password }),
            });

            const data = await response.json();

            if (response.ok) {
                messageEl.textContent = "✅ Signup successful! You can now log in.";
                messageEl.style.color = "green";
                setTimeout(() => (window.location.href = "index.html"), 1500);
            } else {
                messageEl.textContent = data.message || "❌ Signup failed!";
                messageEl.style.color = "red";
            }
        } catch (error) {
            messageEl.textContent = "⚠️ Error connecting to server.";
            messageEl.style.color = "red";
            console.error(error);
        }
    });
}

// =================== RESET PASSWORD ===================
const resetPasswordForm = document.getElementById("resetPasswordForm");
if (resetPasswordForm) {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get("token");

    resetPasswordForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const newPassword = document.getElementById("newPassword").value.trim();
        const confirmPassword = document.getElementById("confirmPassword").value.trim();
        const messageEl = document.getElementById("message");

        if (newPassword !== confirmPassword) {
            messageEl.textContent = "❌ Passwords do not match!";
            messageEl.style.color = "red";
            return;
        }

        try {
            const response = await fetch(`${backendUrl}/auth/reset-password`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ token, newPassword }),
            });

            const data = await response.json();

            if (response.ok) {
                messageEl.textContent = "✅ Password reset successful! Redirecting...";
                messageEl.style.color = "green";
                setTimeout(() => (window.location.href = "index.html"), 2000);
            } else {
                messageEl.textContent = data.message || "❌ Password reset failed.";
                messageEl.style.color = "red";
            }
        } catch (err) {
            messageEl.textContent = "⚠️ Error connecting to server.";
            messageEl.style.color = "red";
            console.error(err);
        }
    });
}
