import { Link, useLocation } from "react-router-dom";
import { getRouteMeta } from "../../app/router/routeConfig";
export function Breadcrumb() {
  const { pathname } = useLocation();
  const current = getRouteMeta(pathname);
  const items = current?.breadcrumb || [{ label: current?.title || "Trang" }];
  return (
    <nav className="breadcrumb" aria-label="Breadcrumb">
      <ol>
        {items.map((item, index) => <li key={`${item.label}-${index}`} aria-current={index === items.length - 1 ? "page" : undefined}>
          {item.to && index < items.length - 1 ? <Link to={item.to}>{item.label}</Link> : item.label}
        </li>)}
      </ol>
    </nav>
  );
}
