import React from "react";
import { EuroSymbol } from "./EuroSymbol";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EuroSymbol",
  component: EuroSymbol,
};

export const Default = () => <EuroSymbol />;
export const Fill = () => <EuroSymbol fill="blue" />;
export const Size = () => <EuroSymbol height="50" width="50" />;
export const CustomStyle = () => <EuroSymbol style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EuroSymbol className="custom-class" />;
