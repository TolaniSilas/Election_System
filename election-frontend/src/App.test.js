import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders election operations heading", () => {
  render(<App />);
  expect(screen.getByText(/Election System/i)).toBeInTheDocument();
});
