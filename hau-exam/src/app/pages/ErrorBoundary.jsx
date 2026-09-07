import { Component } from "react";
import { ErrorPage } from "./ErrorPage";

export class ErrorBoundary extends Component {
  state = { hasError: false };
  static getDerivedStateFromError() {
    return { hasError: true };
  }
  componentDidCatch(error) {
    console.error("Web render error", error);
  }
  render() {
    return this.state.hasError ? <ErrorPage /> : this.props.children;
  }
}
