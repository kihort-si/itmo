import './App.scss'
import Header from "./components/layout/header/Header.tsx";
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import Footer from "./components/layout/footer/Footer.tsx";
import AuthPage from "./components/pages/login/AuthPage.tsx";
import Profile from "./components/pages/profile/Profile.tsx";
import Catalog from "./components/pages/catalog/Catalog.tsx";
import ProductCard from "./components/pages/catalog/ProductCard.tsx";
import HomePage from "./components/pages/home/HomePage.tsx";
import CreateApplication from "./components/pages/application/CreateApplication.tsx";
import ApplicationDetails from "./components/pages/application/ApplicationDetails.tsx";
import OrderDetails from "./components/pages/order/OrderDetails.tsx";
import ProtectedRoute from "./components/common/ProtectedRoute.tsx";
import AboutPage from "./components/pages/about/AboutPage.tsx";
import DiscountsPage from "./components/pages/discounts/DiscountsPage.tsx";
import LegalPage from "./components/pages/legal/LegalPage.tsx";
import { useUserRole } from "./hooks/useUserRole.ts";
import StaffDashboard from "./components/pages/staff/StaffDashboard.tsx";

function App() {
  const { isStaff, loading } = useUserRole();

  if (loading) {
    return (
      <Router>
        <div className="min-h-screen flex flex-col">
          <Header />
          <main className="flex-1 bg-stone-950 flex items-center justify-center">
            <div className="text-white">Loading...</div>
          </main>
        </div>
      </Router>
    );
  }

  return (
    <Router>
      <div className="min-h-screen flex flex-col">
        <Header />

        <main className="flex-1 bg-stone-950">
          <Routes>
            {!isStaff && (
            <>
              <Route path="/" element={<HomePage/>}/>
              <Route path="/about" element={<AboutPage/>}/>
              <Route path="/discounts" element={<DiscountsPage/>}/>
              <Route path="/legal" element={<LegalPage/>}/>
              <Route path="/catalog" element={<Catalog/>}/>
              <Route path="/catalog/:id" element={<ProductCard/>}/>
              <Route path="/applications/create" element={<CreateApplication/>}/>
              <Route path="/applications/:id" element={<ApplicationDetails/>}/>
              <Route path="/orders/:id" element={<OrderDetails/>}/>
              <Route path="/auth" element={<AuthPage/>}/>
              <Route path="/profile" element={<Profile/>}/>
            </>)}
            {isStaff && (
              <>
                <Route
                  path="/"
                  element={
                    <ProtectedRoute>
                      <StaffDashboard/>
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/applications"
                  element={
                    <ProtectedRoute>
                      <StaffDashboard/>
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/applications/:id"
                  element={
                    <ProtectedRoute>
                      <ApplicationDetails/>
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/orders/:id"
                  element={
                    <ProtectedRoute>
                      <OrderDetails/>
                    </ProtectedRoute>
                  }
                />
              </>
            )}
          </Routes>
        </main>

        {!isStaff && (
          <Footer />
        )}
      </div>
    </Router>
  );
}

export default App
